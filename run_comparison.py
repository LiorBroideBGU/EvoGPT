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


def evaluate_test(project, source_path, test_file, work_dir, extra_cp=None):
    """Evaluate a test file and return metrics dict."""
    evaluator = TestEvaluator(
        project_name=project,
        source_file_path=source_path,
        extra_classpath=extra_cp,
    )
    return evaluator.evaluate(test_file, work_dir)


def print_results_table(results):
    """Print a formatted comparison table."""
    if not results:
        print("No results to display.")
        return

    header = f"{'Project':<20} {'Class':<20} {'Tool':<12} {'Budget':>8} {'Type':<12} {'Branch%':>9} {'Line%':>9} {'Mutation%':>11} {'Time(s)':>9}"
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

        print(f"{r['project']:<20} {r['class']:<20} {r['tool']:<12} {budget_str:>8} {r['budget_type']:<12} {branch:>9} {line:>9} {mut:>11} {elapsed:>9}")

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
  # Single class, generations budget
  python run_comparison.py --class-path benchmarks/gson/src/main/java/com/google/gson/JsonArray.java \\
    --project gson --budget-type generations --budgets 5 10 25

  # Single class, time budget
  python run_comparison.py --class-path benchmarks/gson/src/main/java/com/google/gson/JsonArray.java \\
    --project gson --budget-type time --budgets 30 60 120

  # Batch mode
  python run_comparison.py --batch-config comparison_config.json --budget-type time --budgets 60

  # Download EvoSuite JARs first
  python run_comparison.py --download-evosuite
        """,
    )

    parser.add_argument("--class-path", help="Path to Java source file under test")
    parser.add_argument("--project", help="Project name (e.g., gson)")
    parser.add_argument("--batch-config", help="Path to batch config JSON file")

    parser.add_argument("--budget-type", choices=["generations", "time"], default="generations",
                        help="Budget type: 'generations' or 'time' (seconds). Applied to BOTH tools.")
    parser.add_argument("--budgets", nargs="+", type=int, default=[5, 10, 25],
                        help="Budget values to sweep (applied to both tools)")

    parser.add_argument("--evogpt-population", type=int, default=5,
                        help="EvoGPT initial population size (default: 5)")
    parser.add_argument("--output-dir", default="comparison_results",
                        help="Output directory for results")

    parser.add_argument("--skip-evogpt", action="store_true", help="Skip EvoGPT runs")
    parser.add_argument("--skip-evosuite", action="store_true", help="Skip EvoSuite runs")
    parser.add_argument("--skip-testart", action="store_true", help="Skip TestART baseline runs")
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
    else:
        parser.error("Provide either --class-path + --project, or --batch-config")

    os.makedirs(args.output_dir, exist_ok=True)
    all_results = []

    for target in targets:
        project = target["project"]
        class_path = target["class_path"]
        class_name = os.path.splitext(os.path.basename(class_path))[0]

        # --- TestART baseline (runs once per class, budget-independent) ---
        if not args.skip_testart:
            try:
                test_file, elapsed = run_testart(
                    class_path, project, args.output_dir,
                )
                if test_file:
                    work_dir = os.path.join(args.output_dir, "eval", "testart", project, class_name)
                    metrics = evaluate_test(project, class_path, test_file, work_dir)
                    all_results.append({
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
                    all_results.append({
                        "project": project, "class": class_name, "tool": "TestART",
                        "budget": "N/A", "budget_type": "single",
                        "branch_coverage": None, "line_coverage": None,
                        "mutation_score": None, "elapsed": elapsed,
                    })
            except Exception as e:
                print(f"[TestART] FAILED for {class_name}: {e}")
                import traceback
                traceback.print_exc()
                all_results.append({
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
                        all_results.append({
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
                        all_results.append({
                            "project": project, "class": class_name, "tool": "EvoGPT",
                            "budget": budget, "budget_type": args.budget_type,
                            "branch_coverage": None, "line_coverage": None,
                            "mutation_score": None, "elapsed": elapsed,
                        })
                except Exception as e:
                    print(f"[EvoGPT] FAILED for {class_name} budget={budget}: {e}")
                    import traceback
                    traceback.print_exc()
                    all_results.append({
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
                        metrics = evaluate_test(project, class_path, test_file, work_dir)
                        all_results.append({
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
                        all_results.append({
                            "project": project, "class": class_name, "tool": "EvoSuite",
                            "budget": budget, "budget_type": args.budget_type,
                            "branch_coverage": None, "line_coverage": None,
                            "mutation_score": None, "elapsed": elapsed,
                        })
                except Exception as e:
                    print(f"[EvoSuite] FAILED for {class_name} budget={budget}: {e}")
                    import traceback
                    traceback.print_exc()
                    all_results.append({
                        "project": project, "class": class_name, "tool": "EvoSuite",
                        "budget": budget, "budget_type": args.budget_type,
                        "branch_coverage": None, "line_coverage": None,
                        "mutation_score": None, "elapsed": None,
                    })

    print_results_table(all_results)
    csv_path = os.path.join(args.output_dir, "results.csv")
    save_csv(all_results, csv_path)


if __name__ == "__main__":
    main()
