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
import sys
from pathlib import Path

from config.config_loader import load
from utils.benchmark_utils import ensure_extracted
from logging.handlers import RotatingFileHandler

def _setup_logging() -> logging.Logger:
    """Configure logging and return the main logger."""
    logging.basicConfig(
        level=logging.DEBUG,
        format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
        handlers=[
            RotatingFileHandler(f"{os.getenv('LOGGING_DIRECTORY', 'logs')}/evogpt.log", maxBytes=10*1024*1024),
        ],
    )
    logging.getLogger("httpcore").setLevel(logging.CRITICAL)
    return logging.getLogger(__name__)

global_logger = _setup_logging()

def _parse_args() -> argparse.Namespace:
    """Parse command-line arguments."""
    parser = argparse.ArgumentParser(description="EvoGPT: Evolutionary unit test generation")
    parser.add_argument("--config", default="config/config.json", help="Path to config JSON")
    return parser.parse_args()


def _validate_run_config(cfg: dict) -> None:
    """
    Validate the run section when mode is set.
    Exits with code 1 if validation fails.
    """
    run = cfg.run

    if run.get("output_dir") is None:
        global_logger.error("run.output_dir is required when run.mode is set")
        sys.exit(1)

def _resolve_local_paths(cfg: dict, output_dir: Path) -> Path:
    """
    Resolve class_path and project for local mode.
    Extracts benchmark from benchmarks/<project>.zip to output_dir.
    """
    project_path = ensure_extracted(project_name=cfg.PROJECT, benchmarks_dir="benchmarks", target_dir=output_dir)
    if project_path is None:
        global_logger.error(f"Failed to extract benchmark {cfg.PROJECT} from benchmarks/ to {output_dir}")
        sys.exit(1)

    class_path = project_path / Path(cfg.CLASS_PATH).relative_to("benchmarks")
    return class_path


def _resolve_paths_and_update_config(cfg: dict) -> Path:
    """
    Resolve class_path and project based on run.mode, then update cfg.
    Must run before any module that uses config is imported.
    """
    run = cfg.run

    output_dir = Path(run["output_dir"])
    output_dir.mkdir(parents=True, exist_ok=True)
    return _resolve_local_paths(cfg, output_dir)


def _run_generator(cfg: dict, class_path: Path, output_dir: Path) -> None:
    """Create ChromosomesGenerator and run the evolutionary test generation."""
    from app.chromosomes_generator import ChromosomesGenerator

    generator = ChromosomesGenerator(
        project_name=cfg.PROJECT,
        source_code_path=class_path,
    )
    asyncio.run(
        generator.generate_final_unit_test(
            max_generations=cfg.EVO_GENERATIONS,
            n_chromosomes=cfg.EVO_POPULATION,
            output_path=output_dir,
        )
    )


def main() -> int:
    """Main entry point: load config, resolve paths, run generator."""
    logger = _setup_logging()
    args = _parse_args()

    cfg = load(args.config)
    _validate_run_config(cfg, logger)
    class_path = _resolve_paths_and_update_config(cfg)
    output_dir = Path(cfg.run["output_dir"])

    logger.info("Starting the application")
    _run_generator(cfg, class_path, output_dir)
    logger.info("Application finished")

    return 0


if __name__ == "__main__":
    sys.exit(main())
