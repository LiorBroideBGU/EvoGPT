#!/usr/bin/env python3
"""
EvoGPT vs EvoSuite Comparison Script

Runs both tools under the same budget (generations or wall-clock time),
evaluates with JaCoCo + PITest, and produces a comparative summary.

Usage:
  python run_comparison.py --config config/config.json

All settings (project, class_path, budgets, etc.) are read from the config file.
"""

import argparse
import asyncio
import csv
import gc
import json
import os
import shutil
import sys
import time
import traceback
import logging
from pathlib import Path

from config.config_loader import load, get
from utils.EvoSuiteRunner.evosuite_runner import EvoSuiteRunner, download_evosuite_jars
from utils.test_evaluator import TestEvaluator
from utils.dataset_utils import is_focal_class
from utils.function_utils import read_java_file_as_string
from utils.benchmark_utils import ensure_extracted, find_source_root
from app.chromosomes_generator import ChromosomesGenerator
from logging.handlers import RotatingFileHandler

TOOL_NAME_TO_DIR = {
    "EvoGPT": "evogpt",
    "EvoSuite": "evosuite",
    "EvoGPT+EvoSuite": "evogpt_evosuite",
}

# -----------------------------------------------------------------------------
# Setup
# -----------------------------------------------------------------------------

def _setup_logging() -> logging.Logger:
    """Configure logging and return the main logger."""
    logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s", 
    handlers=[RotatingFileHandler(f"{os.getenv('LOGGING_DIRECTORY', 'logs')}/comparison.log", maxBytes=10*1024*1024)])
    return logging.getLogger(__name__)


def _parse_args() -> argparse.Namespace:
    """Parse command-line arguments."""
    parser = argparse.ArgumentParser(description="EvoGPT vs EvoSuite Comparison")
    parser.add_argument("--config", default="config/config.json", help="Path to config JSON")
    return parser.parse_args()

global_logger = _setup_logging()

# -----------------------------------------------------------------------------
# Target resolution
# -----------------------------------------------------------------------------


def _load_targets_from_batch_config(batch_config_path: str) -> list[dict]:
    """Load targets from a batch config JSON file."""
    with open(batch_config_path, encoding="utf-8") as f:
        data = json.load(f)
    return data["targets"]


def _discover_focal_classes(
    project_name: str, benchmarks_dir: str = "benchmarks", limit: int | None = None, output_dir: Path = Path("evogpt_extracted")
) -> list[str]:
    """
    Walk a benchmark project's source tree and return class paths for every
    focal class. Automatically extracts the project zip if needed.
    """
    project_path = ensure_extracted(project_name, benchmarks_dir, target_dir=output_dir)
    if project_path is None:
        global_logger.error(f"[discover] No folder or zip found for project '{project_name}' under {benchmarks_dir}/")
        sys.exit(1)

    source_root = find_source_root(project_path, project_name)
    focal_paths: list[str] = []
    sorted_java_files = sorted(source_root.rglob("*.java"))
    global_logger.info(f"[discover] Source root: {source_root}")
    for java_file in sorted_java_files:
        code = read_java_file_as_string(java_file)
        if code and is_focal_class(code):
            focal_paths.append(java_file.as_posix())

    global_logger.info(f"[discover] Found {len(focal_paths)} focal classes ({len(sorted_java_files) - len(focal_paths)} skipped — interfaces / abstract / non-public)")
    if limit is not None and limit < len(focal_paths):
        focal_paths = focal_paths[:limit]
        global_logger.info(f"[discover] Limiting to first {limit} classes")

    return focal_paths



def _resolve_targets(comp: dict, output_dir: Path) -> list[dict]:
    """
    Resolve the list of (project, class_path) targets from config.

    Supports: batch_config, class_path+project, or project+discover.
    """
    if comp.get("batch_config"):
        return _load_targets_from_batch_config(comp["batch_config"])

    if comp.get("class_path") and comp.get("project"):
        return [{"project": comp["project"], "class_path": comp["class_path"]}]

    if comp.get("project"):
        focal_paths = _discover_focal_classes(comp["project"], limit=comp.get("limit"), output_dir=output_dir)
        return [{"project": comp["project"], "class_path": p} for p in focal_paths]
    
    global_logger.error("[resolve_targets] Set comparison.project (with optional class_path or limit), or comparison.batch_config in config.json")
    sys.exit(1)


# -----------------------------------------------------------------------------
# Tool runners (EvoGPT, TestART, EvoSuite, Hybrid)
# -----------------------------------------------------------------------------


def _run_async_with_suppressed_io(coro_func) -> None:
    """Run an async function while suppressing stdout/stderr (e.g. from JaCoCo, javac)."""
    _stdout = sys.stdout
    sys.stdout = open(os.devnull, "w")
    try:
        asyncio.run(coro_func())
    finally:
        sys.stdout.close()
        sys.stdout = _stdout

    _stderr = sys.stderr
    sys.stderr = open(os.devnull, "w")
    gc.collect()
    sys.stderr.close()
    sys.stderr = _stderr


def run_evogpt(class_path: str, project: str, budget: int | float, budget_type: str, population: int, output_dir: Path) -> tuple[Path | None, float]:
    """Run EvoGPT and return (test_file_path, elapsed_seconds)."""
    cfg = get()
    cfg.CLASS_PATH = class_path
    cfg.PROJECT = project

    if budget_type == "generations":
        cfg.EVO_GENERATIONS = budget
        cfg.EVO_POPULATION = population
        time_limit = None
    else:
        cfg.EVO_GENERATIONS = 999
        cfg.EVO_POPULATION = population
        time_limit = budget

    gen = ChromosomesGenerator(project_name=project, source_code_path=Path(class_path))

    global_logger.info(f"\n{'='*60}\n[EvoGPT] Running: {class_path} Budget: {budget} ({budget_type}), Population: {population}\n{'='*60}")
    start = time.time()

    async def _run():
        try:
            await gen.generate_final_unit_test(max_generations=cfg.EVO_GENERATIONS, n_chromosomes=population, time_limit_seconds=time_limit)
        finally:
            pending = [t for t in asyncio.all_tasks() if t is not asyncio.current_task()]
            for t in pending:
                t.cancel()
            if pending:
                await asyncio.gather(*pending, return_exceptions=True)

    _run_async_with_suppressed_io(_run)
    elapsed = time.time() - start

    class_name = Path(class_path).stem
    result_test = Path("results", "unit_tests", project, class_name, f"{class_name}Test.java")

    if not result_test.exists():
        global_logger.info(f"[EvoGPT] No test generated at {result_test}")
        return None, elapsed

    dest_dir = Path(output_dir, "evogpt_tests", project, class_name, f"{budget_type}_{budget}")
    dest_dir.mkdir(parents=True, exist_ok=True)
    dest_file = dest_dir / f"{class_name}Test.java"
    shutil.copy(result_test, dest_file)

    source_in_results = Path("results", "unit_tests", project, class_name, f"{class_name}.java")
    if source_in_results.exists():
        shutil.copy(source_in_results, dest_dir / f"{class_name}.java")

    return dest_file, elapsed


def run_testart(class_path: str, project: str, output_dir: Path) -> tuple[Path | None, float]:
    """Run TestART baseline (single LLM generation, no evolution)."""
    cfg = get()
    cfg.CLASS_PATH = class_path
    cfg.PROJECT = project

    gen = ChromosomesGenerator(project_name=project, source_code_path=Path(class_path))
    class_name = gen.class_name

    global_logger.info(f"\n{'='*60}\n[TestART] Running: {class_path}\n{'='*60}")
    start = time.time()

    async def _run():
        try:
            await gen.threaded_generation(thread_number=1, temperature=0.5)
        finally:
            pending = [t for t in asyncio.all_tasks() if t is not asyncio.current_task()]
            for t in pending:
                t.cancel()
            if pending:
                await asyncio.gather(*pending, return_exceptions=True)

    _run_async_with_suppressed_io(_run)
    elapsed = time.time() - start

    if not gen.chromosomes:
        global_logger.info(f"[TestART] No test generated for {class_name}")
        return None, elapsed

    best = gen.chromosomes[0]
    result_test = best.test_file_path

    if not os.path.exists(result_test):
        global_logger.info(f"[TestART] No test file at {result_test}")
        return None, elapsed

    dest_dir = Path(output_dir, "testart_tests", project, class_name)
    dest_dir.mkdir(parents=True, exist_ok=True)
    dest_file = dest_dir / f"{class_name}Test.java"
    shutil.copy(result_test, dest_file)

    source_in_results = Path("results", "unit_tests", project, class_name, f"{class_name}.java")
    if source_in_results.exists():
        shutil.copy(source_in_results, dest_dir / f"{class_name}.java")

    global_logger.info(f"[TestART] Test: {dest_file} ({elapsed:.1f}s)")
    return dest_file, elapsed


def run_evosuite(class_path: str, project: str, budget: int | float, budget_type: str, population: int, output_dir: str) -> tuple[Path | None, float, str | None]:
    """Run EvoSuite and return (test_file_path, elapsed_seconds, runtime_jar)."""
    # population is added just for the sake of consistency with the other functions
    # but it is not used in EvoSuite
    runner = EvoSuiteRunner(project_name=project, class_path_source=class_path, output_base_dir=output_dir)

    global_logger.info(f"\n{'='*60}\n[EvoSuite] Running: {class_path} Budget: {budget} ({budget_type}\n{'='*60}")

    start = time.time()
    test_file = runner.generate_tests(budget=budget, budget_type=budget_type)
    elapsed = time.time() - start

    return test_file, elapsed, runner.get_runtime_jar()


def run_evogpt_seeded(class_path: str, project: str, budget: int | float, budget_type: str, population: int, output_dir: str) -> tuple[Path | None, float, str | None]:
    """Run hybrid: LLM population then EvoSuite with seeds."""
    import re as _re

    cfg = get()
    cfg.CLASS_PATH = class_path
    cfg.PROJECT = project

    gen = ChromosomesGenerator(
        project_name=project, source_code_path=Path(class_path)
    )
    class_name = gen.class_name

    global_logger.info(f"\n{'='*60}\n[EvoGPT+EvoSuite] Running: {class_path} Phase 1: LLM population ({population} threads)\n{'='*60}")
    start = time.time()

    base_temps = [0.3, 0.4, 0.5, 0.6, 0.8]
    temperatures = [base_temps[i % len(base_temps)] for i in range(population)]

    async def _generate_population():
        try:
            tasks = [
                gen.threaded_generation(i + 1, temp)
                for i, temp in enumerate(temperatures)
            ]
            await asyncio.gather(*tasks)
        finally:
            pending = [t for t in asyncio.all_tasks() if t is not asyncio.current_task()]
            for t in pending:
                t.cancel()
            if pending:
                await asyncio.gather(*pending, return_exceptions=True)

    _run_async_with_suppressed_io(_generate_population)
    llm_elapsed = time.time() - start
    global_logger.info(f"[EvoGPT+EvoSuite] Phase 1 done: {len(gen.chromosomes)} tests ({llm_elapsed:.1f}s)")

    if not gen.chromosomes:
        global_logger.info("[EvoGPT+EvoSuite] No LLM tests, falling back to unseeded EvoSuite")
        return run_evosuite(class_path, project, budget, budget_type, output_dir)

    seed_base = output_dir / "evogpt_seeds" / project / class_name
    seed_base.mkdir(parents=True, exist_ok=True)

    pkg = None
    try:
        with open(class_path, encoding="utf-8") as f:
            for line in f:
                m = _re.match(r"^\s*package\s+([\w.]+)\s*;", line)
                if m:
                    pkg = m.group(1)
                    break
    except Exception:
        pass

    seed_dir = seed_base / pkg.replace(".", os.sep) if pkg else seed_base
    seed_dir.mkdir(parents=True, exist_ok=True)

    seeded_count = 0
    for chromo in gen.chromosomes:
        test_path = chromo.test_file_path
        if test_path and test_path.is_file():
            dest = seed_dir / f"{class_name}Test_seed{chromo.thread_id}.java"
            try:
                content = test_path.read_text(encoding="utf-8")
                old_cls = f"{class_name}Test"
                new_cls = f"{class_name}Test_seed{chromo.thread_id}"
                content = content.replace(f"class {old_cls}", f"class {new_cls}")
                dest.write_text(content, encoding="utf-8")
                seeded_count += 1
            except Exception:
                pass

    global_logger.info(f"[EvoGPT+EvoSuite] Phase 2: Seeding EvoSuite with {seeded_count} tests")

    runner = EvoSuiteRunner(project_name=project, class_path_source=class_path, output_base_dir=output_dir)
    global_logger.info(f"[EvoGPT+EvoSuite] Phase 3: EvoSuite evolution (budget={budget} {budget_type})")

    evo_start = time.time()
    test_file = runner.generate_tests(budget=budget, budget_type=budget_type, seed_dir=seed_base)
    total_elapsed = time.time() - start
    global_logger.info(f"[EvoGPT+EvoSuite] Done. LLM: {llm_elapsed:.1f}s, EvoSuite: {time.time()-evo_start:.1f}s, Total: {total_elapsed:.1f}s")

    return test_file, total_elapsed, runner.get_runtime_jar()


# -----------------------------------------------------------------------------
# Evaluation and results
# -----------------------------------------------------------------------------


def _evaluate_test(project: str, source_path: str, test_file: Path, work_dir: str, extra_cp: str | None = None) -> dict | None:
    """Evaluate a test file and return metrics dict."""
    evaluator = TestEvaluator(project_name=project, source_file_path=source_path, extra_classpath=extra_cp)
    return evaluator.evaluate(test_file, work_dir)


def _create_result_row(project: str, class_name: str, tool: str, budget: str | int | float, budget_type: str, metrics: dict | None, elapsed: float | None) -> dict:
    """Build a result row dict from metrics and elapsed time."""
    return {
        "project": project,
        "class": class_name,
        "tool": tool,
        "budget": budget,
        "budget_type": budget_type,
        "branch_coverage": metrics["branch_coverage"] if metrics else None,
        "line_coverage": metrics["line_coverage"] if metrics else None,
        "mutation_score": metrics["test_strength"] if metrics else None,
        "elapsed": elapsed,
    }


def _create_failed_row(project: str, class_name: str, tool: str, budget: str | int | float, budget_type: str) -> dict:
    """Build a result row for a failed tool run."""
    return _create_result_row(project, class_name, tool, budget, budget_type, metrics=None, elapsed=None)


def _save_csv(results: list[dict], results_path: Path) -> None:
    """Write results to CSV."""
    fieldnames = [
        "project", "class", "tool", "budget", "budget_type",
        "branch_coverage", "line_coverage", "mutation_score", "elapsed",
    ]
    with open(results_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(results)
    global_logger.info(f"Results saved to: {results_path}")


def _print_results_table(results: list[dict]) -> None:
    """Print a formatted comparison table."""
    if not results:
        print("No results to display.")
        return

    header = f"{'Project':<20} {'Class':<20} {'Tool':<18} {'Budget':>8} {'Type':<12} {'Branch%':>9} {'Line%':>9} {'Mutation%':>11} {'Time(s)':>9}"
    sep = "-" * len(header)

    global_logger.info(f"\n{'='*len(header)}")
    global_logger.info("COMPARISON RESULTS")
    global_logger.info(f"{'='*len(header)}")
    global_logger.info(header)
    global_logger.info(sep)

    prev_key = None
    for r in results:
        key = (r["project"], r["class"])
        if prev_key and prev_key != key:
            print(sep)
        prev_key = key

        branch = f"{r['branch_coverage']:.1f}" if r["branch_coverage"] is not None else "N/A"
        line = f"{r['line_coverage']:.1f}" if r["line_coverage"] is not None else "N/A"
        mut = f"{r['mutation_score']:.1f}" if r["mutation_score"] is not None else "N/A"
        elapsed = f"{r['elapsed']:.1f}" if r["elapsed"] is not None else "N/A"
        budget_str = str(r["budget"])

        print(f"{r['project']:<20} {r['class']:<20} {r['tool']:<18} {budget_str:>8} {r['budget_type']:<12} {branch:>9} {line:>9} {mut:>11} {elapsed:>9}")

    print(f"{'='*len(header)}\n")


# -----------------------------------------------------------------------------
# Target processing
# -----------------------------------------------------------------------------


def _run_and_record_testart(target: dict, output_dir: Path, record_fn) -> None:
    """Run TestART for a target and record the result."""
    project = target["project"]
    class_path = target["class_path"]
    class_name = Path(class_path).stem

    try:
        test_file, elapsed = run_testart(class_path, project, output_dir)
        work_dir = output_dir / "eval" / "testart" / project / class_name
        if test_file:
            metrics = _evaluate_test(project, class_path, test_file, work_dir)
            row = _create_result_row(project, class_name, "TestART", "N/A", "single", metrics, elapsed)
        else:
            row = _create_result_row(project, class_name, "TestART", "N/A", "single", None, elapsed)
        record_fn(row)
    except Exception as e:
        global_logger.error(f"[TestART] FAILED for {class_name}: {e}")
        traceback.print_exc()
        record_fn(_create_failed_row(project, class_name, "TestART", "N/A", "single"))


def _run_and_record_budget_tool(
    target: dict,
    budget: int | float,
    comp: dict,
    output_dir: Path,
    tool_name: str,
    population: int,
    run_fn,
    record_fn,
) -> None:
    """Run a budget-dependent tool (EvoGPT, EvoSuite, Hybrid) and record the result."""
    project = target["project"]
    class_path = target["class_path"]
    class_name = Path(class_path).stem
    budget_type = comp["budget_type"]

    try:
        result = run_fn(class_path, project, budget, budget_type, population, output_dir)
        if len(result) == 3:
            test_file, elapsed, _runtime_jar = result
        else:
            test_file, elapsed = result
        
        work_dir = output_dir / "eval" / TOOL_NAME_TO_DIR[tool_name] / project / class_name / f"{budget_type}_{budget}"

        if test_file:
            compiled_source_dir = output_dir / "evosuite_runs" / project / class_name / f"{budget_type}_{budget}" / "compiled_source"
            extra_cp = compiled_source_dir if compiled_source_dir.exists() else None
            metrics = _evaluate_test(project, class_path, test_file, work_dir, extra_cp=extra_cp)
            row = _create_result_row(project, class_name, tool_name, budget, budget_type, metrics, elapsed)
        else:
            row = _create_result_row(project, class_name, tool_name, budget, budget_type, None, elapsed)
        record_fn(row)
    
    except Exception as e:
        global_logger.error(f"[{tool_name}] FAILED for {class_name} budget={budget}: {e}")
        traceback.print_exc()
        record_fn(_create_failed_row(project, class_name, tool_name, budget, budget_type))


def _process_target(target: dict, comp: dict, output_dir: Path, record_fn) -> None:
    """Process a single target: run all tools and record results."""

    if not comp.get("skip_testart"):
        _run_and_record_testart(target, output_dir, record_fn)

    for budget in comp["budgets"]:
        if not comp.get("skip_evogpt"):
            _run_and_record_budget_tool(
                target, budget, comp, output_dir, "EvoGPT", comp["evogpt_population"], run_evogpt, record_fn
            )
        if not comp.get("skip_evosuite"):
            _run_and_record_budget_tool(
                target, budget, comp, output_dir, "EvoSuite", 0, run_evosuite, record_fn
            )
        if not comp.get("skip_hybrid"):
            _run_and_record_budget_tool(
                target, budget, comp, output_dir, "EvoGPT+EvoSuite", comp["hybrid_population"], run_evogpt_seeded, record_fn
            )


# -----------------------------------------------------------------------------
# Main orchestration
# -----------------------------------------------------------------------------


def main() -> int:
    """Main entry point: resolve targets, run comparison, save results."""
    args = _parse_args()
    load(args.config)
    cfg = get()
    comp = cfg.comparison
    output_dir = Path(cfg.run["output_dir"])
    results_dir = output_dir / "results"
    output_dir.mkdir(parents=True, exist_ok=True)
    results_dir.mkdir(parents=True, exist_ok=True)
    if comp.get("download_evosuite"):
        download_evosuite_jars(force=True)
        global_logger.info("EvoSuite JARs downloaded successfully.")

    targets = _resolve_targets(comp, output_dir)
    all_results: list[dict] = []

    def record(row: dict) -> None:
        all_results.append(row)
        _save_csv(all_results, results_dir / "results.csv")

    for idx, target in enumerate(targets, 1):
        class_name = Path(target["class_path"]).stem
        global_logger.info(f"\n[{idx}/{len(targets)}] Processing {class_name} ...")
        _process_target(target, comp, output_dir, record)

    _print_results_table(all_results)


if __name__ == "__main__":
    main()
