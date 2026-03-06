"""
EvoGPT: Evolutionary unit test generation.

Usage:
  python main.py --config config/config.json

All settings (mode, url, output_dir, class_path) are read from the config file.
"""

import os
import argparse
import asyncio
import logging
import subprocess
import sys
from pathlib import Path

from config.config_loader import load
from utils.benchmark_utils import ensure_extracted


def _setup_logging() -> logging.Logger:
    """Configure logging and return the main logger."""
    logging.basicConfig(
        level=logging.DEBUG,
        format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
        handlers=[
            logging.RotatingFileHandler(f"{os.getenv('LOGGING_DIRECTORY', 'logs')}/evogpt.log", maxBytes=10*1024*1024, backupCount=5),
        ],
    )
    logging.getLogger("httpcore").setLevel(logging.CRITICAL)
    return logging.getLogger(__name__)


def _parse_args() -> argparse.Namespace:
    """Parse command-line arguments."""
    parser = argparse.ArgumentParser(description="EvoGPT: Evolutionary unit test generation")
    parser.add_argument("--config", default="config/config.json", help="Path to config JSON")
    return parser.parse_args()


def _validate_run_config(cfg, logger: logging.Logger) -> None:
    """
    Validate the run section when mode is set.
    Exits with code 1 if validation fails.
    """
    run = cfg.run
    mode = run.get("mode")
    if mode is None:
        return

    if run.get("output_dir") is None:
        logger.error("run.output_dir is required when run.mode is set")
        sys.exit(1)

    if mode == "remote" and not run.get("url"):
        logger.error("run.url is required when run.mode=remote")
        sys.exit(1)


def _resolve_direct_paths(cfg) -> tuple[Path, str]:
    """
    Resolve class_path and project when mode is None (direct benchmark path).
    Uses cfg.CLASS_PATH and cfg.PROJECT as-is.
    """
    return Path(cfg.CLASS_PATH), cfg.PROJECT


def _resolve_local_paths(cfg, output_dir: Path, logger: logging.Logger) -> tuple[Path, str]:
    """
    Resolve class_path and project for local mode.
    Extracts benchmark from benchmarks/<project>.zip to output_dir.
    """
    if not ensure_extracted(cfg.PROJECT, benchmarks_dir="benchmarks", target_dir=output_dir):
        logger.error(f"Failed to extract benchmark {cfg.PROJECT} from benchmarks/ to {output_dir}")
        sys.exit(1)

    if cfg.run.get("class_path"):
        class_path = Path(cfg.run["class_path"])
    else:
        class_path = output_dir / Path(cfg.CLASS_PATH).relative_to("benchmarks")

    return class_path, cfg.PROJECT


def _resolve_remote_paths(cfg, output_dir: Path, logger: logging.Logger) -> tuple[Path, str]:
    """
    Resolve class_path and project for remote mode.
    Clones the repository from run.url to output_dir.
    """
    url = cfg.run.get("url", "")
    try:
        subprocess.run(["git", "clone", url, str(output_dir)], check=True, capture_output=True)
    except subprocess.CalledProcessError as e:
        logger.error(f"git clone failed: {e.stderr.decode() if e.stderr else e}")
        sys.exit(1)

    if cfg.run.get("class_path"):
        class_path = Path(cfg.run["class_path"])
    else:
        class_path = output_dir / Path(cfg.CLASS_PATH).relative_to("benchmarks")

    project = Path(url).stem.removesuffix(".git") if url else cfg.PROJECT
    return class_path, project


def _resolve_paths_and_update_config(cfg, logger: logging.Logger) -> None:
    """
    Resolve class_path and project based on run.mode, then update cfg.
    Must run before any module that uses config is imported.
    """
    run = cfg.run
    mode = run.get("mode")

    if mode is None:
        class_path, project = _resolve_direct_paths(cfg)

    else:
        output_dir = Path(run["output_dir"])
        output_dir.mkdir(parents=True, exist_ok=True)

        if mode == "local":
            class_path, project = _resolve_local_paths(cfg, output_dir, logger)
        else:
            class_path, project = _resolve_remote_paths(cfg, output_dir, logger)

        cfg.CLASS_PATH = str(class_path)
        cfg.PROJECT = project
        cfg.PROJECT_ROOT = str(output_dir)


def _run_generator(cfg) -> None:
    """Create ChromosomesGenerator and run the evolutionary test generation."""
    from app.chromosomes_generator import ChromosomesGenerator

    generator = ChromosomesGenerator(
        project_name=cfg.PROJECT,
        source_code_path=Path(cfg.CLASS_PATH),
    )
    asyncio.run(
        generator.generate_final_unit_test(
            max_generations=cfg.EVO_GENERATIONS,
            n_chromosomes=cfg.EVO_POPULATION,
        )
    )


def main() -> int:
    """Main entry point: load config, resolve paths, run generator."""
    logger = _setup_logging()
    args = _parse_args()

    cfg = load(args.config)
    _validate_run_config(cfg, logger)
    _resolve_paths_and_update_config(cfg, logger)

    logger.info("Starting the application")
    _run_generator(cfg)
    logger.info("Application finished")

    return 0


if __name__ == "__main__":
    sys.exit(main())
