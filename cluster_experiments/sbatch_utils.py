"""
Shared utilities for generating sbatch job scripts from the template.
"""

from pathlib import Path
from typing import Any


def get_template_path() -> Path:
    """Return the path to the template.sbatch file."""
    return Path(__file__).resolve().parent / "template.sbatch"


def get_project_root() -> Path:
    """Return the EvoGPT project root directory."""
    return Path(__file__).resolve().parent.parent


def load_template() -> str:
    """Load the template.sbatch content."""
    path = get_template_path()
    return path.read_text(encoding="utf-8")


def substitute_template(
    content: str,
    *,
    job_name: str,
    script: str,
    arguments: str,
    work_dir: str | Path | None = None,
    cpus_per_task: int = 4,
    mem: str = "16G",
    conda_env: str = "evogpt",
    cluster_temp_logs_path: str = "/tmp/evogpt_logs",
    logs_dir: str = "logs",
    error_enabled: bool = True,
    dependency: str | None = None,
    job_info_print: str = "",
    environment_variables: str = "",
) -> str:
    """
    Substitute placeholders in the template content.

    :param content: Raw template content.
    :param job_name: SLURM job name.
    :param script: Python script path (e.g. main.py).
    :param arguments: Arguments to pass to the script.
    :param work_dir: Working directory for the job (default: project root).
    :param cpus_per_task: Number of CPUs.
    :param mem: Memory allocation.
    :param conda_env: Conda environment name.
    :param cluster_temp_logs_path: Temporary logs path on cluster node.
    :param logs_dir: Final logs directory (relative or absolute).
    :param error_enabled: Whether to enable --error sbatch option.
    :param dependency: Job dependency (e.g. afterok:123).
    :param job_info_print: Extra echo lines for job info.
    :param environment_variables: Environment variable exports.
    :return: Substituted content.
    """
    if work_dir is None:
        # Use SLURM submit dir so jobs work regardless of where project lives on cluster
        work_dir = "${SLURM_SUBMIT_DIR}"

    work_dir = str(work_dir)

    # When error_enabled=False, we comment out the #SBATCH --error line
    error_prefix = "" if error_enabled else "# "
    dependency_exists = ""
    dep_value = ""
    if dependency:
        dependency_exists = ""
        dep_value = dependency
    else:
        dependency_exists = "# "
        dep_value = ""

    replacements: dict[str, Any] = {
        "job_name": job_name,
        "cpus_per_task": cpus_per_task,
        "mem": mem,
        "error": error_prefix,
        "dependency_exists": dependency_exists,
        "dependency": dep_value,
        "job_info_print": job_info_print,
        "environment_variables": environment_variables,
        "cluster_temp_logs_path": cluster_temp_logs_path,
        "conda_env": conda_env,
        "work_dir": work_dir,
        "script": script,
        "arguments": arguments,
        "logs_dir": logs_dir,
    }

    result = content
    for key, value in replacements.items():
        result = result.replace("${" + key + "}", str(value))
    return result


def write_sbatch(output_path: Path, content: str) -> None:
    """Write the generated sbatch content to a file."""
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(content, encoding="utf-8")
