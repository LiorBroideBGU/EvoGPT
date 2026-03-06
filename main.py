"""
EvoGPT: Evolutionary unit test generation.

Usage:
  python main.py --config config/config.json

All settings (mode, url, output_dir, class_path) are read from the config file.
"""

import argparse
import asyncio
import logging
import subprocess
import sys
from pathlib import Path

from config.config_loader import load
from utils.benchmark_utils import ensure_extracted

if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG,
                        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    logger = logging.getLogger(__name__)
    logging.getLogger("httpcore").setLevel(logging.CRITICAL)

    parser = argparse.ArgumentParser(description='EvoGPT: Evolutionary unit test generation')
    parser.add_argument('--config', default='config/config.json', help='Path to config JSON')
    args = parser.parse_args()

    # Load config first (before any module that uses config is imported)
    cfg = load(args.config)

    run = cfg.run
    mode = run.get("mode")

    # Validate run section when mode is set
    if mode is not None:
        if run.get("output_dir") is None:
            logger.error('run.output_dir is required when run.mode is set')
            sys.exit(1)
        if mode == "remote" and not run.get("url"):
            logger.error('run.url is required when run.mode=remote')
            sys.exit(1)

    # Resolve class_path and project; mutate config before importing ChromosomesGenerator
    if mode is None:
        class_path = Path(cfg.CLASS_PATH)
        project = cfg.PROJECT
    else:
        output_dir = Path(run["output_dir"])
        output_dir.mkdir(parents=True, exist_ok=True)

        if mode == "local":
            if not ensure_extracted(cfg.PROJECT, benchmarks_dir='benchmarks', target_dir=output_dir):
                logger.error(f'Failed to extract benchmark {cfg.PROJECT} from benchmarks/ to {output_dir}')
                sys.exit(1)
            if run.get("class_path"):
                class_path = Path(run["class_path"])
            else:
                class_path = output_dir / Path(cfg.CLASS_PATH).relative_to('benchmarks')
            project = cfg.PROJECT
            cfg.PROJECT_ROOT = str(output_dir)

        else:  # remote
            try:
                subprocess.run(['git', 'clone', run["url"], str(output_dir)], check=True, capture_output=True)
            except subprocess.CalledProcessError as e:
                logger.error(f'git clone failed: {e.stderr.decode() if e.stderr else e}')
                sys.exit(1)
            if run.get("class_path"):
                class_path = Path(run["class_path"])
            else:
                class_path = output_dir / Path(cfg.CLASS_PATH).relative_to('benchmarks')
            project = Path(run["url"]).stem.removesuffix('.git') if run.get("url") else cfg.PROJECT
            cfg.PROJECT_ROOT = str(output_dir)

        cfg.CLASS_PATH = str(class_path)
        cfg.PROJECT = project

    logger.info("Starting the application")
    from app.chromosomes_generator import ChromosomesGenerator
    chromosomes_generator = ChromosomesGenerator(project_name=cfg.PROJECT, source_code_path=Path(cfg.CLASS_PATH))
    asyncio.run(chromosomes_generator.generate_final_unit_test(max_generations=cfg.EVO_GENERATIONS, n_chromosomes=cfg.EVO_POPULATION))
    logger.info("Application finished")
