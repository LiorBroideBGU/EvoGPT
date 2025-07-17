from config.config import *
from app.chromosomes_generator import ChromosomesGenerator
import asyncio

if __name__ == '__main__':

    chromosomes_generator = ChromosomesGenerator(project_name=PROJECT, source_code_path=CLASS_PATH)
    asyncio.run(chromosomes_generator.generate_final_unit_test(max_time=EVO_TIME_LIMIT, n_chromosomes=EVO_POPULATION))
