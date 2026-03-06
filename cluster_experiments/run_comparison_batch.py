#!/usr/bin/env python3
"""
Generate sbatch jobs that run run_comparison.py on each benchmark project.

Creates one job per project in benchmarks/. Each job runs the full comparison
(EvoGPT, EvoSuite, TestART, Hybrid) on all focal classes without skipping any.

Usage:
  python run_comparison_batch.py [--output-dir DIR] [--submit]
  python run_comparison_batch.py --projects gson,commons-lang --submit

Flow:
  1. Resolve project list (from --projects or benchmarks/)
  2. Create JSON config per project  ← JSON creation
  3. Create sbatch file per project  ← sbatch creation
  4. Optionally submit jobs         ← execution
"""

import argparse
import json
import subprocess
import sys
from pathlib import Path

# Add project root to path
_SCRIPT_DIR = Path(__file__).resolve().parent
_PROJECT_ROOT = _SCRIPT_DIR.parent
sys.path.insert(0, str(_PROJECT_ROOT))

from cluster_experiments.sbatch_utils import (
    get_project_root,
    load_template,
    substitute_template,
    write_sbatch,
)


# -----------------------------------------------------------------------------
# Step 1: Project discovery
# -----------------------------------------------------------------------------


def _discover_benchmark_projects(benchmarks_dir: str = "benchmarks") -> list[str]:
    """
    Discover all benchmark projects (folders or .zip files) in the benchmarks directory.
    """
    bench_path = Path(benchmarks_dir)
    if not bench_path.exists():
        return []

    projects: list[str] = []
    for item in sorted(bench_path.iterdir()):
        if item.is_dir() or (item.is_file() and item.suffix == ".zip"):
            projects.append(item.stem if item.suffix == ".zip" else item.name)
    return sorted(set(projects))


def _resolve_projects(projects_arg: str | None, benchmarks_path: Path) -> list[str]:
    """
    Resolve the list of projects to process.

    :param projects_arg: Comma-separated project names, or None to discover all.
    :param benchmarks_path: Path to benchmarks directory.
    :return: Sorted list of project names.
    """
    if projects_arg:
        return [p.strip() for p in projects_arg.split(",") if p.strip()]
    return _discover_benchmark_projects(str(benchmarks_path))


# -----------------------------------------------------------------------------
# Step 2: JSON config creation
# -----------------------------------------------------------------------------


def _create_comparison_config_json(
    base_config_path: Path,
    output_config_path: Path,
    project: str,
    comparison_output_dir: str,
) -> None:
    """
    Create a project-specific JSON config for run_comparison.

    Sets comparison.project and ensures no tools are skipped
    (skip_evogpt, skip_evosuite, skip_testart, skip_hybrid = false).
    """
    with open(base_config_path, encoding="utf-8") as f:
        cfg = json.load(f)

    comp = cfg.setdefault("comparison", {})
    comp["project"] = project
    comp["class_path"] = None
    comp["limit"] = None
    comp["batch_config"] = None
    comp["output_dir"] = comparison_output_dir.replace("\\", "/")
    comp["skip_evogpt"] = False
    comp["skip_evosuite"] = False
    comp["skip_testart"] = False
    comp["skip_hybrid"] = False
    comp["results_dir"] = str((output_config_path.parent / "comparison_results").resolve().as_posix())

    output_config_path.parent.mkdir(parents=True, exist_ok=True)
    with open(output_config_path, "w", encoding="utf-8") as f:
        json.dump(cfg, f, indent=2)


# -----------------------------------------------------------------------------
# Step 3: sbatch file creation
# -----------------------------------------------------------------------------


def _logs_dir_relative_to_project(output_dir: Path, subdir: str, project_root: Path) -> str:
    """
    Compute logs directory path, relative to project root when possible.

    Used for cluster portability: when the job runs, it cd's to project root.
    """
    logs_path = output_dir / "logs" / subdir
    try:
        return logs_path.resolve().relative_to(project_root.resolve()).as_posix()
    except ValueError:
        return str(logs_path)


def _create_sbatch_file(
    project: str,
    config_path: Path,
    output_dir: Path,
    project_root: Path,
    *,
    cpus: int,
    mem: str,
    conda_env: str,
    dependency: str | None,
) -> Path:
    """
    Create the sbatch job file for a comparison run.

    :return: Path to the created sbatch file.
    """
    try:
        config_rel = config_path.resolve().relative_to(project_root.resolve()).as_posix()
    except ValueError:
        config_rel = Path(config_path).as_posix()

    content = substitute_template(
        load_template(),
        job_name=f"comparison-{project}",
        script="run_comparison.py",
        arguments=f"--config {config_rel}",
        work_dir=str(Path(__file__).resolve().parent.parent.absolute()),
        cpus_per_task=cpus,
        mem=mem,
        conda_env=conda_env,
        cluster_temp_logs_path=f"/tmp/comparison_{project}_${{SLURM_JOB_ID}}",
        logs_dir=_logs_dir_relative_to_project(output_dir, f"comparison_{project}", project_root),
        dependency=dependency,
    )

    sbatch_path = output_dir / f"comparison_{project}.sbatch"
    write_sbatch(sbatch_path, content)
    return sbatch_path


# -----------------------------------------------------------------------------
# Step 4: Job submission (execution)
# -----------------------------------------------------------------------------


def _submit_job(sbatch_path: Path, project_root: Path) -> tuple[int, str | None]:
    """
    Submit the sbatch job to the cluster.

    :return: (exit_code, job_id or None on failure).
    """
    result = subprocess.run(
        ["sbatch", "--parsable", str(sbatch_path)],
        cwd=str(project_root),
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        return result.returncode, None
    job_id = result.stdout.strip().split(";")[0]
    return 0, job_id


# -----------------------------------------------------------------------------
# Main orchestration
# -----------------------------------------------------------------------------


def _parse_args() -> argparse.Namespace:
    """Parse and return command-line arguments."""
    parser = argparse.ArgumentParser(description="Generate sbatch jobs for run_comparison on each benchmark project")
    parser.add_argument("--projects", help="Comma-separated project names. If omitted, uses all projects in benchmarks/")
    parser.add_argument("--config", default="config/config.json", help="Base config file path")
    parser.add_argument(
        "--output-dir", type=Path, default=_SCRIPT_DIR / "jobs", help="Directory for generated sbatch and config files")
    parser.add_argument(
        "--comparison-output-dir", default="comparison_results", help="Base output directory for comparison results (per-project subdirs)")
    parser.add_argument(
        "--conda-env", default="evogpt", help="Conda environment name")
    parser.add_argument("--submit", action="store_true", help="Submit all jobs with sbatch after generating")
    parser.add_argument("--sequential", action="store_true", help="Add job dependencies so jobs run one after another")
    parser.add_argument("--cpus", type=int, default=4, help="CPUs per task")
    parser.add_argument("--mem", default="16G", help="Memory allocation")
    return parser.parse_args()


def main() -> int:
    """
    Main entry point: generate sbatch + config per project, optionally submit.

    Flow:
      1. Resolve projects
      2. Create JSON config per project
      3. Create sbatch file per project
      4. Optionally submit
    """
    args = _parse_args()
    project_root = get_project_root()
    base_config = project_root / args.config

    if not base_config.exists():
        print(f"ERROR: Base config not found: {base_config}")
        return 1

    projects = _resolve_projects(args.projects, project_root / "benchmarks")
    if not projects:
        print("ERROR: No projects found in benchmarks/")
        return 1

    output_dir = args.output_dir.resolve()
    configs_dir = output_dir / "configs"
    job_ids: list[str] = []

    for project in projects:
        # Step 2: Create JSON config
        config_path = configs_dir / f"config_comparison_{project}.json"
        comparison_out = (Path(args.comparison_output_dir) / project).as_posix()
        _create_comparison_config_json(base_config, config_path, project, comparison_out)

        # Step 3: Create sbatch file
        dependency = f"afterok:{':'.join(job_ids)}" if args.sequential and job_ids else None
        sbatch_path = _create_sbatch_file(
            project,
            config_path,
            output_dir,
            project_root,
            cpus=args.cpus,
            mem=args.mem,
            conda_env=args.conda_env,
            dependency=dependency,
        )
        print(f"Generated: {sbatch_path}")

        # Step 4: Optionally submit
        if args.submit:
            exit_code, job_id = _submit_job(sbatch_path, project_root)
            if exit_code != 0:
                print(f"ERROR submitting {project}")
                return 1
            job_ids.append(job_id)
            print(f"  Submitted: job {job_id}")

    print(f"\nTotal: {len(projects)} jobs for projects: {', '.join(projects)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
