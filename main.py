from config.config import *
from app.chromosomes_generator import ChromosomesGenerator
import asyncio
import logging

if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG,
                        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    logger = logging.getLogger(__name__)
    logging.getLogger("httpcore").setLevel(logging.CRITICAL)
    logger.info("Starting the application")
    chromosomes_generator = ChromosomesGenerator(project_name=PROJECT, source_code_path=CLASS_PATH)
    asyncio.run(chromosomes_generator.generate_final_unit_test(max_generations=EVO_GENERATIONS, n_chromosomes=EVO_POPULATION))
    logger.info("Application finished")