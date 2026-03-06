#!/usr/bin/env python3
"""
Generate an sbatch job that runs main.py on a single benchmark project.

Usage:
  python run_single_project.py --project gson --class-path benchmarks/gson/src/main/java/.../X.java
  python run_single_project.py --project gson --class-path PATH [--submit]

Assumption: Benchmarks are zipped (e.g. benchmarks/gson.zip) before execution.
Discovery of focal classes requires extracted sources, so --class-path is required.
main.py will extract the benchmark at runtime (run.mode="local") before running.

Flow:
  1. Validate and normalize class path (must be under benchmarks/)
  2. Create project-specific JSON config with run.mode=local  ← JSON creation
  3. Create sbatch file from template                       ← sbatch creation
  4. Optionally submit the job                              ← execution
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
# Step 1: Class path validation and normalization
# -----------------------------------------------------------------------------


def _validate_and_normalize_class_path(
    class_path_arg: str,
    project_root: Path,
) -> str:
    """
    Validate and normalize the Java class path.

    The path must be under benchmarks/ so main.py can resolve it after
    extraction (output_dir / path.relative_to('benchmarks')).

    :param class_path_arg: User-provided class path (e.g. benchmarks/gson/.../X.java).
    :param project_root: EvoGPT project root directory.
    :return: Normalized path with forward slashes, relative to project root.
    :raises SystemExit: If path is invalid or not under benchmarks/.
    """
    p = Path(class_path_arg)
    if p.is_absolute():
        try:
            normalized = p.relative_to(project_root).as_posix()
        except ValueError:
            print(
                f"ERROR: Class path must be under project root or benchmarks/: {class_path_arg}"
            )
            sys.exit(1)
    else:
        normalized = Path(class_path_arg).as_posix()

    if not normalized.startswith("benchmarks/"):
        print(
            f"ERROR: Class path must start with 'benchmarks/' so main.py can resolve it "
            f"after extraction. Got: {normalized}"
        )
        sys.exit(1)

    return normalized


# -----------------------------------------------------------------------------
# Step 2: JSON config creation
# -----------------------------------------------------------------------------


def _create_project_config_json(
    base_config_path: Path,
    output_config_path: Path,
    project: str,
    class_path: str,
) -> None:
    """
    Create a project-specific JSON config file.

    Loads the base config, overrides PROJECT and CLASS_PATH, and configures
    run.mode="local" with run.output_dir so main.py extracts the benchmark
    from benchmarks/<project>.zip at runtime before running.

    :param base_config_path: Path to config/config.json (or similar).
    :param output_config_path: Where to write the project config (e.g. jobs/configs/config_gson.json).
    :param project: Project name to set in config.
    :param class_path: Class path under benchmarks/ (e.g. benchmarks/gson/.../X.java).
    """
    with open(base_config_path, encoding="utf-8") as f:
        cfg = json.load(f)

    cfg["PROJECT"] = project
    cfg["CLASS_PATH"] = class_path

    # main.py extracts benchmarks when run.mode="local"; required when benchmarks are zipped
    cfg.setdefault("run", {})
    cfg["run"]["mode"] = "local"
    cfg["run"]["output_dir"] = str((output_config_path.parent.parent / "evogpt_extracted").resolve().as_posix())
    cfg["results_dir"] = str((output_config_path.parent.parent / "results").resolve().as_posix())

    output_config_path.parent.mkdir(parents=True, exist_ok=True)
    with open(output_config_path, "w", encoding="utf-8") as f:
        json.dump(cfg, f, indent=2)


# -----------------------------------------------------------------------------
# Step 3: sbatch file creation
# -----------------------------------------------------------------------------


def _logs_dir_relative_to_project(
    output_dir: Path, project: str, project_root: Path
) -> str:
    """
    Compute logs directory path, relative to project root when possible.

    Used for cluster portability: when the job runs, it cd's to project root,
    so the cp command needs a path relative to that.

    :param output_dir: Base output directory (e.g. cluster_experiments/jobs).
    :param project: Project name.
    :param project_root: EvoGPT project root.
    :return: Path string with forward slashes.
    """
    logs_path = output_dir / "logs" / project
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
    conda_env: str,
    cpus: int,
    mem: str,
) -> Path:
    """
    Create the sbatch job file from the template.

    Substitutes placeholders in template.sbatch with project-specific values.
    The generated file runs: cd work_dir && python main.py --config <config>.

    :param project: Project name (e.g. gson).
    :param config_path: Path to the project config JSON.
    :param output_dir: Directory for generated files (e.g. cluster_experiments/jobs).
    :param project_root: EvoGPT project root.
    :param work_dir: Working directory on cluster (default: ${SLURM_SUBMIT_DIR}).
    :param conda_env: Conda environment name.
    :param cpus: CPUs per task.
    :param mem: Memory allocation (e.g. 16G).
    :return: Path to the created sbatch file.
    """
    try:
        config_rel = config_path.resolve().relative_to(project_root.resolve()).as_posix()
    except ValueError:
        config_rel = Path(config_path).as_posix()

    template = load_template()
    content = substitute_template(
        template,
        job_name=f"evogpt-{project}",
        script="main.py",
        arguments=f"--config {config_rel}",
        work_dir=str(Path(__file__).resolve().parent.parent.absolute()),
        cpus_per_task=cpus,
        mem=mem,
        conda_env=conda_env,
        cluster_temp_logs_path=f"/tmp/evogpt_{project}_${{SLURM_JOB_ID}}",
        logs_dir=_logs_dir_relative_to_project(output_dir, project, project_root),
    )

    sbatch_path = output_dir / f"run_{project}.sbatch"
    write_sbatch(sbatch_path, content)
    return sbatch_path


# -----------------------------------------------------------------------------
# Step 4: Job submission (execution)
# -----------------------------------------------------------------------------


def _submit_job(sbatch_path: Path, project_root: Path) -> int:
    """
    Submit the sbatch job to the cluster.

    Runs `sbatch <sbatch_path>` from the project root so SLURM_SUBMIT_DIR
    is set correctly when the job starts.

    :param sbatch_path: Path to the .sbatch file.
    :param project_root: Directory to run sbatch from.
    :return: Exit code from sbatch (0 on success).
    """
    result = subprocess.run(
        ["sbatch", str(sbatch_path)],
        cwd=str(project_root),
    )
    return result.returncode


# -----------------------------------------------------------------------------
# Main orchestration
# -----------------------------------------------------------------------------


def _parse_args() -> argparse.Namespace:
    """Parse and return command-line arguments."""
    parser = argparse.ArgumentParser(
        description="Generate sbatch job for running main.py on a single project"
    )
    parser.add_argument(
        "--project",
        required=True,
        help="Benchmark project name (e.g. gson, commons-lang)",
    )
    parser.add_argument(
        "--class-path",
        required=True,
        help="Path to the Java class file under benchmarks/ (e.g. benchmarks/gson/src/main/java/.../X.java). "
        "Required because benchmarks are zipped before execution; discovery is not possible.",
    )
    parser.add_argument("--config", default="config/config.json", help="Path to config JSON")
    parser.add_argument("--output-dir", type=Path, default=_SCRIPT_DIR / "jobs", help="Directory for generated sbatch and config files")
    parser.add_argument(
        "--work-dir", help="Working directory on cluster (default: ${SLURM_SUBMIT_DIR})")
    parser.add_argument("--conda-env", default="evogpt", help="Conda environment name")
    parser.add_argument(
        "--submit", action="store_true", help="Submit the job with sbatch after generating")
    parser.add_argument("--cpus", type=int, default=4, help="CPUs per task")
    parser.add_argument(
        "--mem", default="16G", help="Memory allocation")
    return parser.parse_args()


def main() -> int:
    """
    Main entry point: generate sbatch + config, optionally submit.

    Flow:
      1. Resolve class path
      2. Create JSON config
      3. Create sbatch file
      4. Optionally submit
    """
    args = _parse_args()
    project_root = get_project_root()
    base_config = project_root / args.config

    if not base_config.exists():
        print(f"ERROR: Base config not found: {base_config}")
        return 1

    # Step 1: Validate and normalize class path
    class_path = _validate_and_normalize_class_path(args.class_path, project_root)
    output_dir = args.output_dir.resolve()
    config_path = output_dir / "configs" / f"config_{args.project}.json"

    # Step 2: Create JSON config
    _create_project_config_json(base_config, config_path, args.project, class_path)

    # Step 3: Create sbatch file
    sbatch_path = _create_sbatch_file(args.project, config_path, output_dir, project_root, 
    conda_env=args.conda_env, cpus=args.cpus, mem=args.mem)

    print(f"Generated: {sbatch_path}")
    print(f"Config: {config_path}")
    print(f"Project: {args.project}, Class: {Path(class_path).name}")

    # Step 4: Optionally submit
    if args.submit:
        return _submit_job(sbatch_path, project_root)

    return 0


if __name__ == "__main__":
    sys.exit(main())
