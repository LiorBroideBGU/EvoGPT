"""
Shared utilities for benchmark extraction and source root discovery.
Used by main.py (local mode) and run_comparison.py (discover_focal_classes).
"""

import logging
import zipfile
from pathlib import Path

logger = logging.getLogger(__name__)


def ensure_extracted(project_name: str, benchmarks_dir: str = "benchmarks", target_dir: str | Path | None = None) -> bool:
    """
    Extract the project zip if the folder doesn't already exist.

    :param project_name: Name of the project (e.g. 'gson').
    :param benchmarks_dir: Directory containing the project zip (e.g. benchmarks/{project_name}.zip).
    :param target_dir: Where to extract. If None, uses benchmarks_dir (backward compatible).
    :return: True if project folder exists or extraction succeeded, False otherwise.
    """
    target = Path(target_dir) if target_dir is not None else Path(benchmarks_dir)
    project_path = target / project_name

    if project_path.is_dir():
        return True

    zip_path = Path(benchmarks_dir) / f"{project_name}.zip"
    if not zip_path.is_file():
        return False

    logger.info(f"[discover] Extracting {zip_path} ...")
    with zipfile.ZipFile(zip_path, "r") as zf:
        zf.extractall(target)
        
    return project_path.is_dir()


def find_source_root(project_path: Path, project_name: str) -> str | None:
    """
    Locate the src/main/java root for a given benchmark project.

    :param project_path: Path to the extracted project folder (e.g. output_dir/gson or benchmarks/gson).
    :param project_name: Name of the project (for special cases like mockito, closure-compiler).
    :return: Path to src/main/java as string, or None if not found.
    """
    if not project_path.is_dir():
        return None

    candidates = [
        project_path / "src" / "main" / "java",
        project_path / "src" / "java",
    ]
    if project_name == "mockito":
        candidates.insert(0, project_path / "mockito-core" / "src" / "main" / "java")
    if project_name == "closure-compiler":
        candidates.append(project_path / "src")

    for path in candidates:
        if path.is_dir():
            return str(path)
    return None
