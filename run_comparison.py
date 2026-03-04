#!/usr/bin/env python3
"""
EvoGPT vs EvoSuite Comparison Script

Runs both tools under the same budget (generations or wall-clock time),
evaluates with JaCoCo + PITest, and produces a comparative summary.
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

from utils.EvoSuiteRunner.evosuite_runner import EvoSuiteRunner, download_evosuite_jars
from utils.test_evaluator import TestEvaluator
from utils.dataset_utils import is_focal_class
from utils.function_utils import read_java_file_as_string


def run_evogpt(class_path, project, budget, budget_type, population, output_dir):
    """
    Run EvoGPT and return the path to the best test file + its metrics.
    """
    import config.config as cfg
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

    from app.chromosomes_generator import ChromosomesGenerator

    gen = ChromosomesGenerator(project_name=project, source_code_path=class_path, verbose=False)

    print(f"\n{'='*60}")
    print(f"[EvoGPT] Running: {class_path}")
    print(f"[EvoGPT] Budget: {budget} ({budget_type}), Population: {population}")
    print(f"{'='*60}")

    start = time.time()

    async def _run():
        try:
            await gen.generate_final_unit_test(
                max_generations=cfg.EVO_GENERATIONS,
                n_chromosomes=population,
                time_limit_seconds=time_limit,
            )
        finally:
            pending = [t for t in asyncio.all_tasks() if t is not asyncio.current_task()]
            for t in pending:
                t.cancel()
            if pending:
                await asyncio.gather(*pending, return_exceptions=True)

    # Suppress all stdout from EvoGPT internals (JaCoCo, javac, chromosome, etc.)
    _stdout = sys.stdout
    sys.stdout = open(os.devnull, 'w')
    try:
        asyncio.run(_run())
    finally:
        sys.stdout.close()
        sys.stdout = _stdout
    # Force GC with stderr suppressed to silence httpx "Event loop is closed" tracebacks
    _stderr = sys.stderr
    sys.stderr = open(os.devnull, 'w')
    gc.collect()
    sys.stderr.close()
    sys.stderr = _stderr

    elapsed = time.time() - start

    class_name = os.path.splitext(os.path.basename(class_path))[0]
    result_test = os.path.join("results", "unit_tests", project, class_name, f"{class_name}Test.java")

    if not os.path.exists(result_test):
        print(f"[EvoGPT] No test generated at {result_test}")
        return None, elapsed

    dest_dir = os.path.join(output_dir, "evogpt_tests", project, class_name, f"{budget_type}_{budget}")
    os.makedirs(dest_dir, exist_ok=True)
    dest_file = os.path.join(dest_dir, f"{class_name}Test.java")
    shutil.copy(result_test, dest_file)

    source_in_results = os.path.join("results", "unit_tests", project, class_name, f"{class_name}.java")
    if os.path.exists(source_in_results):
        shutil.copy(source_in_results, os.path.join(dest_dir, f"{class_name}.java"))

    return dest_file, elapsed


def run_testart(class_path, project, output_dir):
    """
    Run TestART baseline: a single LLM test generation (no evolution).
    Uses the same generation pipeline as one EvoGPT chromosome.
    """
    import config.config as cfg
    cfg.CLASS_PATH = class_path
    cfg.PROJECT = project

    from app.chromosomes_generator import ChromosomesGenerator

    gen = ChromosomesGenerator(project_name=project, source_code_path=class_path, verbose=False)
    class_name = gen.class_name

    print(f"\n{'='*60}")
    print(f"[TestART] Running: {class_path}")
    print(f"{'='*60}")

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

    _stdout = sys.stdout
    sys.stdout = open(os.devnull, 'w')
    try:
        asyncio.run(_run())
    finally:
        sys.stdout.close()
        sys.stdout = _stdout
    _stderr = sys.stderr
    sys.stderr = open(os.devnull, 'w')
    gc.collect()
    sys.stderr.close()
    sys.stderr = _stderr

    elapsed = time.time() - start

    if not gen.chromosomes:
        print(f"[TestART] No test generated for {class_name}")
        return None, elapsed

    best = gen.chromosomes[0]
    result_test = best.test_file_path

    if not os.path.exists(result_test):
        print(f"[TestART] No test file at {result_test}")
        return None, elapsed

    dest_dir = os.path.join(output_dir, "testart_tests", project, class_name)
    os.makedirs(dest_dir, exist_ok=True)
    dest_file = os.path.join(dest_dir, f"{class_name}Test.java")
    shutil.copy(result_test, dest_file)

    source_in_results = os.path.join("results", "unit_tests", project, class_name, f"{class_name}.java")
    if os.path.exists(source_in_results):
        shutil.copy(source_in_results, os.path.join(dest_dir, f"{class_name}.java"))

    print(f"[TestART] Test: {dest_file} ({elapsed:.1f}s)")
    return dest_file, elapsed


def run_evosuite(class_path, project, budget, budget_type, output_dir):
    """
    Run EvoSuite and return the path to the generated test file.
    """
    runner = EvoSuiteRunner(
        project_name=project,
        class_path_source=class_path,
        output_base_dir=output_dir,
    )

    print(f"\n{'='*60}")
    print(f"[EvoSuite] Running: {class_path}")
    print(f"[EvoSuite] Budget: {budget} ({budget_type})")
    print(f"{'='*60}")

    start = time.time()
    test_file = runner.generate_tests(budget=budget, budget_type=budget_type)
    elapsed = time.time() - start

    return test_file, elapsed, runner.get_runtime_jar()


def run_evogpt_seeded(class_path, project, budget, budget_type, population, output_dir):
    """
    Hybrid approach: generate an initial population with EvoGPT's LLM threads,
    then seed EvoSuite's evolutionary algorithm with those tests.
    """
    import config.config as cfg
    import re as _re
    cfg.CLASS_PATH = class_path
    cfg.PROJECT = project

    from app.chromosomes_generator import ChromosomesGenerator

    gen = ChromosomesGenerator(project_name=project, source_code_path=class_path, verbose=False)
    class_name = gen.class_name

    print(f"\n{'='*60}")
    print(f"[EvoGPT+EvoSuite] Running: {class_path}")
    print(f"[EvoGPT+EvoSuite] Phase 1: LLM population ({population} threads)")
    print(f"{'='*60}")

    start = time.time()

    # Phase 1: Generate initial LLM population (no evolution)
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

    _stdout = sys.stdout
    sys.stdout = open(os.devnull, 'w')
    try:
        asyncio.run(_generate_population())
    finally:
        sys.stdout.close()
        sys.stdout = _stdout
    _stderr = sys.stderr
    sys.stderr = open(os.devnull, 'w')
    gc.collect()
    sys.stderr.close()
    sys.stderr = _stderr

    llm_elapsed = time.time() - start
    print(f"[EvoGPT+EvoSuite] Phase 1 done: {len(gen.chromosomes)} tests generated ({llm_elapsed:.1f}s)")

    if not gen.chromosomes:
        print("[EvoGPT+EvoSuite] No LLM tests generated, falling back to unseeded EvoSuite")
        return run_evosuite(class_path, project, budget, budget_type, output_dir)

    # Phase 2: Collect LLM tests into a seed directory with package structure
    seed_base = os.path.join(output_dir, "evogpt_seeds", project, class_name)
    os.makedirs(seed_base, exist_ok=True)

    # Detect package from source file
    pkg = None
    try:
        with open(class_path, 'r') as f:
            for line in f:
                m = _re.match(r'^\s*package\s+([\w.]+)\s*;', line)
                if m:
                    pkg = m.group(1)
                    break
    except Exception:
        pass

    if pkg:
        seed_dir = os.path.join(seed_base, pkg.replace('.', os.sep))
    else:
        seed_dir = seed_base
    os.makedirs(seed_dir, exist_ok=True)

    seeded_count = 0
    for chromo in gen.chromosomes:
        test_path = chromo.test_file_path
        if test_path and os.path.isfile(test_path):
            dest = os.path.join(seed_dir, f"{class_name}Test_seed{chromo.thread_id}.java")
            try:
                content = open(test_path, 'r').read()
                # Rename the class so each seed has a unique class name
                old_cls = f"{class_name}Test"
                new_cls = f"{class_name}Test_seed{chromo.thread_id}"
                content = content.replace(f"class {old_cls}", f"class {new_cls}")
                with open(dest, 'w') as f:
                    f.write(content)
                seeded_count += 1
            except Exception:
                pass

    print(f"[EvoGPT+EvoSuite] Phase 2: Seeding EvoSuite with {seeded_count} LLM tests")

    # Phase 3: Run EvoSuite with seeds
    runner = EvoSuiteRunner(
        project_name=project,
        class_path_source=class_path,
        output_base_dir=output_dir,
    )

    print(f"[EvoGPT+EvoSuite] Phase 3: EvoSuite evolution (budget={budget} {budget_type})")

    evo_start = time.time()
    test_file = runner.generate_tests(
        budget=budget, budget_type=budget_type, seed_dir=seed_base,
    )
    evo_elapsed = time.time() - evo_start
    total_elapsed = time.time() - start

    print(f"[EvoGPT+EvoSuite] Done. LLM: {llm_elapsed:.1f}s, EvoSuite: {evo_elapsed:.1f}s, Total: {total_elapsed:.1f}s")

    return test_file, total_elapsed, runner.get_runtime_jar()


def evaluate_test(project, source_path, test_file, work_dir, extra_cp=None):
    """Evaluate a test file and return metrics dict."""
    evaluator = TestEvaluator(
        project_name=project,
        source_file_path=source_path,
        extra_classpath=extra_cp,
    )
    return evaluator.evaluate(test_file, work_dir)


def _ensure_extracted(project_name, benchmarks_dir="benchmarks"):
    """Extract the project zip if the folder doesn't already exist."""
    project_path = os.path.join(benchmarks_dir, project_name)
    if os.path.isdir(project_path):
        return True

    zip_path = os.path.join(benchmarks_dir, f"{project_name}.zip")
    if not os.path.isfile(zip_path):
        return False

    import zipfile
    print(f"[discover] Extracting {zip_path} ...")
    with zipfile.ZipFile(zip_path, "r") as zf:
        zf.extractall(benchmarks_dir)
    return os.path.isdir(project_path)


def _find_source_root(project_name, benchmarks_dir="benchmarks"):
    """Locate the src/main/java root for a given benchmark project."""
    project_path = os.path.join(benchmarks_dir, project_name)
    if not os.path.isdir(project_path):
        return None

    candidates = [
        os.path.join(project_path, "src", "main", "java"),
        os.path.join(project_path, "src", "java"),
    ]
    if project_name == "mockito":
        candidates.insert(0, os.path.join(project_path, "mockito-core", "src", "main", "java"))
    if project_name == "closure-compiler":
        candidates.append(os.path.join(project_path, "src"))

    for path in candidates:
        if os.path.isdir(path):
            return path
    return None


def discover_focal_classes(project_name, benchmarks_dir="benchmarks", limit=None):
    """
    Walk a benchmark project's source tree and return class paths for every
    focal class (public, non-abstract, non-interface, with real public methods).
    Automatically extracts the project zip if the folder doesn't exist yet.
    """
    if not _ensure_extracted(project_name, benchmarks_dir):
        print(f"[discover] No folder or zip found for project '{project_name}' under {benchmarks_dir}/")
        return []

    source_root = _find_source_root(project_name, benchmarks_dir)
    if source_root is None:
        print(f"[discover] Could not find source root for project '{project_name}' under {benchmarks_dir}/")
        return []

    focal_paths = []
    skipped = 0
    for root, _dirs, files in os.walk(source_root):
        for fname in sorted(files):
            if not fname.endswith(".java"):
                continue
            fpath = os.path.join(root, fname)
            code = read_java_file_as_string(fpath)
            if code and is_focal_class(code):
                focal_paths.append(fpath)
            else:
                skipped += 1

    print(f"[discover] {project_name}: found {len(focal_paths)} focal classes "
          f"({skipped} skipped — interfaces / abstract / non-public)")

    if limit is not None and limit < len(focal_paths):
        focal_paths = focal_paths[:limit]
        print(f"[discover] Limiting to first {limit} classes")

    return focal_paths


def print_results_table(results):
    """Print a formatted comparison table."""
    if not results:
        print("No results to display.")
        return

    header = f"{'Project':<20} {'Class':<20} {'Tool':<18} {'Budget':>8} {'Type':<12} {'Branch%':>9} {'Line%':>9} {'Mutation%':>11} {'Time(s)':>9}"
    sep = "-" * len(header)

    print(f"\n{'='*len(header)}")
    print("COMPARISON RESULTS")
    print(f"{'='*len(header)}")
    print(header)
    print(sep)

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
        budget_str = str(r['budget'])

        print(f"{r['project']:<20} {r['class']:<20} {r['tool']:<18} {budget_str:>8} {r['budget_type']:<12} {branch:>9} {line:>9} {mut:>11} {elapsed:>9}")

    print(f"{'='*len(header)}\n")


def save_csv(results, output_path):
    """Write results to CSV."""
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    fieldnames = [
        "project", "class", "tool", "budget", "budget_type",
        "branch_coverage", "line_coverage", "mutation_score", "elapsed",
    ]
    with open(output_path, "w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(results)
    print(f"Results saved to: {output_path}")


def main():
    parser = argparse.ArgumentParser(
        description="EvoGPT vs EvoSuite Comparison",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # All focal classes in a project
  python run_comparison.py --project jackson-core --budget-type time --budgets 60

  # Limit to 10 focal classes
  python run_comparison.py --project jackson-core --limit 10 --budget-type generations --budgets 5

  # Single class
  python run_comparison.py --class-path benchmarks/gson/src/main/java/com/google/gson/JsonArray.java \\
    --project gson --budget-type generations --budgets 5 10 25

  # Batch mode
  python run_comparison.py --batch-config comparison_config.json --budget-type time --budgets 60

  # Download EvoSuite JARs first
  python run_comparison.py --download-evosuite
        """,
    )

    parser.add_argument("--class-path", help="Path to Java source file under test")
    parser.add_argument("--project", help="Project name (e.g., gson, jackson-core). "
                        "When used without --class-path, discovers all focal classes in the project.")
    parser.add_argument("--limit", type=int, default=None,
                        help="Max number of focal classes to run when discovering a whole project (default: all)")
    parser.add_argument("--batch-config", help="Path to batch config JSON file")

    parser.add_argument("--budget-type", choices=["generations", "time"], default="generations",
                        help="Budget type: 'generations' or 'time' (seconds). Applied to BOTH tools.")
    parser.add_argument("--budgets", nargs="+", type=int, default=[5, 10, 25],
                        help="Budget values to sweep (applied to both tools)")

    parser.add_argument("--evogpt-population", type=int, default=25,
                        help="EvoGPT initial population size (default: 5)")
    parser.add_argument("--output-dir", default="comparison_results",
                        help="Output directory for results")

    parser.add_argument("--skip-evogpt", action="store_true", help="Skip EvoGPT runs")
    parser.add_argument("--skip-evosuite", action="store_true", help="Skip EvoSuite runs")
    parser.add_argument("--skip-testart", action="store_true", help="Skip TestART baseline runs")
    parser.add_argument("--skip-hybrid", action="store_true", help="Skip EvoGPT+EvoSuite hybrid runs")
    parser.add_argument("--hybrid-population", type=int, default=5,
                        help="Number of LLM threads for EvoGPT+EvoSuite hybrid seeding (default: 5)")
    parser.add_argument("--download-evosuite", action="store_true",
                        help="Download EvoSuite JARs and exit")

    args = parser.parse_args()

    if args.download_evosuite:
        download_evosuite_jars(force=True)
        print("EvoSuite JARs downloaded successfully.")
        return

    targets = []
    if args.batch_config:
        with open(args.batch_config) as f:
            config = json.load(f)
        targets = config["targets"]
    elif args.class_path and args.project:
        targets = [{"project": args.project, "class_path": args.class_path}]
    elif args.project:
        focal_paths = discover_focal_classes(args.project, limit=args.limit)
        if not focal_paths:
            parser.error(f"No focal classes found for project '{args.project}'")
        targets = [{"project": args.project, "class_path": p} for p in focal_paths]
    else:
        parser.error("Provide --project (optionally with --class-path or --limit), or --batch-config")

    os.makedirs(args.output_dir, exist_ok=True)
    csv_path = os.path.join(args.output_dir, "results.csv")
    all_results = []

    def _record(row):
        all_results.append(row)
        save_csv(all_results, csv_path)

    for idx, target in enumerate(targets, 1):
        project = target["project"]
        class_path = target["class_path"]
        class_name = os.path.splitext(os.path.basename(class_path))[0]
        print(f"\n[{idx}/{len(targets)}] Processing {class_name} ...")

        # --- TestART baseline (runs once per class, budget-independent) ---
        if not args.skip_testart:
            try:
                test_file, elapsed = run_testart(
                    class_path, project, args.output_dir,
                )
                if test_file:
                    work_dir = os.path.join(args.output_dir, "eval", "testart", project, class_name)
                    metrics = evaluate_test(project, class_path, test_file, work_dir)
                    _record({
                        "project": project,
                        "class": class_name,
                        "tool": "TestART",
                        "budget": "N/A",
                        "budget_type": "single",
                        "branch_coverage": metrics["branch_coverage"] if metrics else None,
                        "line_coverage": metrics["line_coverage"] if metrics else None,
                        "mutation_score": metrics["test_strength"] if metrics else None,
                        "elapsed": elapsed,
                    })
                else:
                    _record({
                        "project": project, "class": class_name, "tool": "TestART",
                        "budget": "N/A", "budget_type": "single",
                        "branch_coverage": None, "line_coverage": None,
                        "mutation_score": None, "elapsed": elapsed,
                    })
            except Exception as e:
                print(f"[TestART] FAILED for {class_name}: {e}")
                import traceback
                traceback.print_exc()
                _record({
                    "project": project, "class": class_name, "tool": "TestART",
                    "budget": "N/A", "budget_type": "single",
                    "branch_coverage": None, "line_coverage": None,
                    "mutation_score": None, "elapsed": None,
                })

        for budget in args.budgets:
            # --- EvoGPT ---
            if not args.skip_evogpt:
                try:
                    test_file, elapsed = run_evogpt(
                        class_path, project, budget, args.budget_type,
                        args.evogpt_population, args.output_dir,
                    )
                    if test_file:
                        work_dir = os.path.join(args.output_dir, "eval", "evogpt", project, class_name, f"{args.budget_type}_{budget}")
                        metrics = evaluate_test(project, class_path, test_file, work_dir)
                        _record({
                            "project": project,
                            "class": class_name,
                            "tool": "EvoGPT",
                            "budget": budget,
                            "budget_type": args.budget_type,
                            "branch_coverage": metrics["branch_coverage"] if metrics else None,
                            "line_coverage": metrics["line_coverage"] if metrics else None,
                            "mutation_score": metrics["test_strength"] if metrics else None,
                            "elapsed": elapsed,
                        })
                    else:
                        _record({
                            "project": project, "class": class_name, "tool": "EvoGPT",
                            "budget": budget, "budget_type": args.budget_type,
                            "branch_coverage": None, "line_coverage": None,
                            "mutation_score": None, "elapsed": elapsed,
                        })
                except Exception as e:
                    print(f"[EvoGPT] FAILED for {class_name} budget={budget}: {e}")
                    import traceback
                    traceback.print_exc()
                    _record({
                        "project": project, "class": class_name, "tool": "EvoGPT",
                        "budget": budget, "budget_type": args.budget_type,
                        "branch_coverage": None, "line_coverage": None,
                        "mutation_score": None, "elapsed": None,
                    })

            # --- EvoSuite ---
            if not args.skip_evosuite:
                try:
                    test_file, elapsed, runtime_jar = run_evosuite(
                        class_path, project, budget, args.budget_type, args.output_dir,
                    )
                    if test_file:
                        work_dir = os.path.join(args.output_dir, "eval", "evosuite", project, class_name, f"{args.budget_type}_{budget}")
                        compiled_source_dir = os.path.join(
                            args.output_dir, "evosuite_runs", project, class_name,
                            f"{args.budget_type}_{budget}", "compiled_source"
                        )
                        evo_extra_cp = compiled_source_dir if os.path.isdir(compiled_source_dir) else None
                        metrics = evaluate_test(project, class_path, test_file, work_dir, extra_cp=evo_extra_cp)
                        _record({
                            "project": project,
                            "class": class_name,
                            "tool": "EvoSuite",
                            "budget": budget,
                            "budget_type": args.budget_type,
                            "branch_coverage": metrics["branch_coverage"] if metrics else None,
                            "line_coverage": metrics["line_coverage"] if metrics else None,
                            "mutation_score": metrics["test_strength"] if metrics else None,
                            "elapsed": elapsed,
                        })
                    else:
                        _record({
                            "project": project, "class": class_name, "tool": "EvoSuite",
                            "budget": budget, "budget_type": args.budget_type,
                            "branch_coverage": None, "line_coverage": None,
                            "mutation_score": None, "elapsed": elapsed,
                        })
                except Exception as e:
                    print(f"[EvoSuite] FAILED for {class_name} budget={budget}: {e}")
                    import traceback
                    traceback.print_exc()
                    _record({
                        "project": project, "class": class_name, "tool": "EvoSuite",
                        "budget": budget, "budget_type": args.budget_type,
                        "branch_coverage": None, "line_coverage": None,
                        "mutation_score": None, "elapsed": None,
                    })

            # --- EvoGPT+EvoSuite Hybrid ---
            if not args.skip_hybrid:
                try:
                    test_file, elapsed, runtime_jar = run_evogpt_seeded(
                        class_path, project, budget, args.budget_type,
                        args.hybrid_population, args.output_dir,
                    )
                    if test_file:
                        work_dir = os.path.join(args.output_dir, "eval", "hybrid", project, class_name, f"{args.budget_type}_{budget}")
                        compiled_source_dir = os.path.join(
                            args.output_dir, "evosuite_runs", project, class_name,
                            f"{args.budget_type}_{budget}", "compiled_source"
                        )
                        hybrid_extra_cp = compiled_source_dir if os.path.isdir(compiled_source_dir) else None
                        metrics = evaluate_test(project, class_path, test_file, work_dir, extra_cp=hybrid_extra_cp)
                        _record({
                            "project": project,
                            "class": class_name,
                            "tool": "EvoGPT+EvoSuite",
                            "budget": budget,
                            "budget_type": args.budget_type,
                            "branch_coverage": metrics["branch_coverage"] if metrics else None,
                            "line_coverage": metrics["line_coverage"] if metrics else None,
                            "mutation_score": metrics["test_strength"] if metrics else None,
                            "elapsed": elapsed,
                        })
                    else:
                        _record({
                            "project": project, "class": class_name, "tool": "EvoGPT+EvoSuite",
                            "budget": budget, "budget_type": args.budget_type,
                            "branch_coverage": None, "line_coverage": None,
                            "mutation_score": None, "elapsed": elapsed,
                        })
                except Exception as e:
                    print(f"[EvoGPT+EvoSuite] FAILED for {class_name} budget={budget}: {e}")
                    import traceback
                    traceback.print_exc()
                    _record({
                        "project": project, "class": class_name, "tool": "EvoGPT+EvoSuite",
                        "budget": budget, "budget_type": args.budget_type,
                        "branch_coverage": None, "line_coverage": None,
                        "mutation_score": None, "elapsed": None,
                    })

    print_results_table(all_results)


if __name__ == "__main__":
    main()
