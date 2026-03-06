#!/usr/bin/env python3
"""
Generate an sbatch job that runs main.py on a single benchmark project.

Usage:
  python run_single_project.py --project gson [--class-path PATH] [--output-dir DIR]
  python run_single_project.py --project gson --submit

Creates a job that runs EvoGPT (main.py) on the given project. If --class-path
is not provided, discovers the first focal class in the project.
"""

import argparse
import json
import os
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


def _discover_first_focal_class(project_name: str, benchmarks_dir: str = "benchmarks") -> str | None:
    """
    Discover the first focal class in the project.
    Returns the absolute path to the class file, or None if not found.
    """
    try:
        from utils.benchmark_utils import ensure_extracted, find_source_root
        from utils.dataset_utils import is_focal_class
        from utils.function_utils import read_java_file_as_string
    except ImportError as e:
        print(f"Note: Cannot discover focal classes (missing dependency: {e}). Use --class-path.")
        return None

    if not ensure_extracted(project_name, benchmarks_dir, target_dir=None):
        return None

    project_path = Path(benchmarks_dir) / project_name
    source_root = find_source_root(project_path, project_name)
    if source_root is None:
        return None

    for root, _dirs, files in os.walk(source_root):
        for fname in sorted(files):
            if not fname.endswith(".java"):
                continue
            fpath = Path(root) / fname
            code = read_java_file_as_string(str(fpath))
            if code and is_focal_class(code):
                return str(fpath.resolve())
    return None


def _logs_dir_for_output(output_dir: Path, project: str, project_root: Path) -> str:
    """Return logs dir path, relative to project root when possible (for cluster portability)."""
    logs_path = output_dir / "logs" / project
    try:
        return logs_path.resolve().relative_to(project_root.resolve()).as_posix()
    except ValueError:
        return str(logs_path)


def _create_project_config(
    base_config_path: Path,
    output_config_path: Path,
    project: str,
    class_path: str,
) -> None:
    """Create a project-specific config file."""
    with open(base_config_path, encoding="utf-8") as f:
        cfg = json.load(f)

    cfg["PROJECT"] = project
    cfg["CLASS_PATH"] = class_path

    output_config_path.parent.mkdir(parents=True, exist_ok=True)
    with open(output_config_path, "w", encoding="utf-8") as f:
        json.dump(cfg, f, indent=2)


def main() -> int:
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
        help="Path to the Java class file. If omitted, discovers first focal class.",
    )
    parser.add_argument(
        "--config",
        default="config/config.json",
        help="Base config file path (default: config/config.json)",
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=_SCRIPT_DIR / "jobs",
        help="Directory for generated sbatch and config files",
    )
    parser.add_argument(
        "--work-dir",
        help="Working directory on cluster (default: project root)",
    )
    parser.add_argument(
        "--conda-env",
        default="evogpt",
        help="Conda environment name",
    )
    parser.add_argument(
        "--submit",
        action="store_true",
        help="Submit the job with sbatch after generating",
    )
    parser.add_argument(
        "--cpus",
        type=int,
        default=4,
        help="CPUs per task",
    )
    parser.add_argument(
        "--mem",
        default="16G",
        help="Memory allocation",
    )
    args = parser.parse_args()

    project_root = get_project_root()
    base_config = project_root / args.config
    if not base_config.exists():
        print(f"ERROR: Base config not found: {base_config}")
        return 1

    class_path = args.class_path
    if not class_path:
        os.chdir(project_root)
        class_path = _discover_first_focal_class(args.project)
        if not class_path:
            print(
                f"ERROR: No focal class found for project '{args.project}'. "
                "Provide --class-path explicitly."
            )
            return 1
        # Use path relative to project root for portability (forward slashes for Linux)
        try:
            class_path = Path(class_path).relative_to(project_root).as_posix()
        except ValueError:
            class_path = str(class_path)
    else:
        # Normalize to forward slashes for cluster (Linux)
        p = Path(class_path)
        if p.is_absolute():
            try:
                class_path = p.relative_to(project_root).as_posix()
            except ValueError:
                class_path = p.as_posix()
        else:
            class_path = Path(class_path).as_posix()

    output_dir = args.output_dir.resolve()
    configs_dir = output_dir / "configs"
    config_path = configs_dir / f"config_{args.project}.json"
    _create_project_config(base_config, config_path, args.project, class_path)

    try:
        config_rel = config_path.resolve().relative_to(project_root.resolve()).as_posix()
    except ValueError:
        config_rel = Path(config_path).as_posix()

    template = load_template()
    job_name = f"evogpt-{args.project}"
    content = substitute_template(
        template,
        job_name=job_name,
        script="main.py",
        arguments=f"--config {config_rel}",
        work_dir=args.work_dir or "${SLURM_SUBMIT_DIR}",
        cpus_per_task=args.cpus,
        mem=args.mem,
        conda_env=args.conda_env,
        cluster_temp_logs_path=f"/tmp/evogpt_{args.project}_${{SLURM_JOB_ID}}",
        logs_dir=_logs_dir_for_output(output_dir, args.project, project_root),
    )

    sbatch_path = output_dir / f"run_{args.project}.sbatch"
    write_sbatch(sbatch_path, content)

    print(f"Generated: {sbatch_path}")
    print(f"Config: {config_path}")
    print(f"Project: {args.project}, Class: {Path(class_path).name}")

    if args.submit:
        import subprocess
        result = subprocess.run(["sbatch", str(sbatch_path)], cwd=str(project_root))
        return result.returncode

    return 0


if __name__ == "__main__":
    sys.exit(main())
