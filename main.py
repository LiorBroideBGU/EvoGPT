from config.config import *
from llm_agents.unit_test_generator import UnitTestGenerator
from llm_agents.coverage_enhancement_agent import CoverageEnhancementAgent
from utils.function_utils import extract_project_name
from utils.java_executor import JavaExecutor
from utils.JavaCodeCoverage.Jacoco import JavaCodeCoverage
from utils.function_utils import *
# from sbst_agents.mutation_assertion_generator import MutationAssertionGenerator
from llm_agents.mutation_assertion_generation_agent import MutationAssertionGenerator
from app.chromosomes_generator import ChromosomesGenerator
from app.chromosome import Chromosome
import threading
from utils.MutationScoreGenerator.PITest import PITestRunner
import asyncio

if __name__ == '__main__':

    chromosomes_generator = ChromosomesGenerator(project_name=PROJECT, source_code_path=CLASS_PATH)
    asyncio.run(chromosomes_generator.generate_final_unit_test(max_time=EVO_TIME_LIMIT, n_chromosomes=EVO_POPULATION))
