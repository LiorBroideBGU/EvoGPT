"""
# Backward compatible (no args)
python main.py

# Local: extract to ./workspace
python main.py --mode local --output-dir ./workspace

# Remote: clone and run
python main.py --mode remote --url https://github.com/google/gson.git --output-dir ./workspace

# Override class path when structure differs
python main.py --mode local --output-dir ./workspace --class-path ./workspace/gson/src/main/java/com/google/gson/JsonArray.java
"""


import argparse
import asyncio
import logging
import subprocess
import sys
from pathlib import Path

import config.config as cfg
from utils.benchmark_utils import ensure_extracted

if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG,
                        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    logger = logging.getLogger(__name__)
    logging.getLogger("httpcore").setLevel(logging.CRITICAL)

    parser = argparse.ArgumentParser(description='EvoGPT: Evolutionary unit test generation')
    parser.add_argument('--mode', choices=['local', 'remote'], help='Use local benchmark or clone from remote URL')
    parser.add_argument('--url', type=str, help='Git clone URL (required when mode=remote)')
    parser.add_argument('--output-dir', type=str, help='Directory for extracted/cloned project (required when mode is set)')
    parser.add_argument('--class-path', type=str, help='Override path to the Java file under test')
    args = parser.parse_args()

    # Validate args when mode is set
    if args.mode is not None:
        if args.output_dir is None:
            logger.error('--output-dir is required when --mode is set')
            sys.exit(1)
        if args.mode == 'remote' and not args.url:
            logger.error('--url is required when --mode=remote')
            sys.exit(1)

    # Resolve class_path and project; mutate config before importing ChromosomesGenerator
    if args.mode is None:
        # Backward compatible: use config as-is
        class_path = Path(cfg.CLASS_PATH)
        project = cfg.PROJECT
    else:
        output_dir = Path(args.output_dir)
        output_dir.mkdir(parents=True, exist_ok=True)

        if args.mode == 'local':
            if not ensure_extracted(cfg.PROJECT, benchmarks_dir='benchmarks', target_dir=output_dir):
                logger.error(f'Failed to extract benchmark {cfg.PROJECT} from benchmarks/ to {output_dir}')
                sys.exit(1)
            if args.class_path:
                class_path = Path(args.class_path)
            else:
                class_path = output_dir / Path(cfg.CLASS_PATH).relative_to('benchmarks')
            project = cfg.PROJECT
            cfg.PROJECT_ROOT = str(output_dir)

        else:  # remote
            # Clone repository
            try:
                subprocess.run(['git', 'clone', args.url, str(output_dir)], check=True, capture_output=True)
            except subprocess.CalledProcessError as e:
                logger.error(f'git clone failed: {e.stderr.decode() if e.stderr else e}')
                sys.exit(1)
            if args.class_path:
                class_path = Path(args.class_path)
            else:
                class_path = output_dir / Path(cfg.CLASS_PATH).relative_to('benchmarks')
            # Derive project name from URL (e.g. https://github.com/google/gson.git -> gson)
            project = Path(args.url).stem.removesuffix('.git') if args.url else cfg.PROJECT
            cfg.PROJECT_ROOT = str(output_dir)

        cfg.CLASS_PATH = str(class_path)
        cfg.PROJECT = project

    logger.info("Starting the application")
    from app.chromosomes_generator import ChromosomesGenerator
    chromosomes_generator = ChromosomesGenerator(project_name=cfg.PROJECT, source_code_path=Path(cfg.CLASS_PATH))
    asyncio.run(chromosomes_generator.generate_final_unit_test(max_generations=cfg.EVO_GENERATIONS, n_chromosomes=cfg.EVO_POPULATION))
    logger.info("Application finished")
