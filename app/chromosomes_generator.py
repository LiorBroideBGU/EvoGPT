from config.config import *
from llm_agents.unit_test_generator import UnitTestGenerator
from llm_agents.coverage_enhancement_agent import CoverageEnhancementAgent
from utils.function_utils import extract_project_name
from utils.java_executor import JavaExecutor
from utils.JavaCodeCoverage.Jacoco import JavaCodeCoverage
from utils.function_utils import *
# from sbst_agents.mutation_assertion_generator import MutationAssertionGenerator
from llm_agents.mutation_assertion_generation_agent import MutationAssertionGenerator
import threading
from app.chromosome import Chromosome

class ChromosomesGenerator:
    def __init__(self, source_code_path, project_name):
        self.source_code_path = source_code_path
        self.project_name = project_name
        self.chromosomes = []
        self.lock = threading.Lock()

    def threaded_generation(self,thread_number, temperature):
        print(f"Thread-{thread_number} starting with temperature={temperature}")

        unit_test_generator = UnitTestGenerator(api_key=API_KEY, model=MODEL, temperature=temperature)
        java_file_path = self.source_code_path
        project_id = self.project_name

        # Generation + Repair loop (initial)
        unit_test_generator.generation_repair_loop(java_file_path=java_file_path, project_id=project_id,
                                                   thread_number=thread_number)

        # Coverage
        jcc = JavaCodeCoverage(
            f'C:\\Users\\liorb\\PycharmProjects\\EvoChat\\results\\unit_tests\\gson\\JsonArray\\{str(thread_number)}\\javafiles',
            'JsonArray',
            'gson',
            thread_id=thread_number
        )
        jcc.generate_coverage_report()
        coverage_metrics, missed_branches = jcc.parse_jacoco_xml(
            rf'C:\Users\liorb\PycharmProjects\EvoChat\results\unit_tests\gson\JsonArray\{str(thread_number)}\classfiles\coverage.xml'
        )
        print("CHECK HERE!!!!!")
        print(coverage_metrics, missed_branches)
        # Enhancements
        test_enhancements = CoverageEnhancementAgent(
            api_key=API_KEY,
            model=MODEL,
            temperature=temperature,
            java_file_path=java_file_path
        )
        test_enhancements.generation_repair_loop(coverage_metrics, missed_branches, thread_number=thread_number)

        # Paths with thread-specific test names
        base_dir = rf'C:\Users\liorb\PycharmProjects\EvoChat\results\unit_tests\gson\JsonArray\{str(thread_number)}\javafiles'
        base_cls_dir = rf'C:\Users\liorb\PycharmProjects\EvoChat\results\unit_tests\gson\JsonArray\{str(thread_number)}\classfiles'
        test1_path = fr'{base_dir}\JsonArrayTest.java'
        test2_path = fr'{base_dir}\JsonArrayEnhancedTest.java'

        first_unit_test = read_java_file_as_string(test1_path)
        enhanced_unit_test = read_java_file_as_string(test2_path)
        final_unit_test = merge_java_unit_tests(first_unit_test, enhanced_unit_test)

        # Cleanup
        delete_file(test1_path)
        delete_file(test2_path)
        delete_file(fr'{base_cls_dir}')

        # Save final merged test
        merged_path = fr'{base_dir}\JsonArrayTest.java'
        save_test_suite(final_unit_test, merged_path)
        chromosome_path = rf'C:\Users\liorb\PycharmProjects\EvoChat\results\unit_tests\{self.project_name}\JsonArray\{str(thread_number)}\javafiles'
        with self.lock:
            chromosome = Chromosome(path=chromosome_path, thread_id=thread_number)
            chromosome.compute_fitness()

        with self.lock:
            self.chromosomes.append(chromosome)
        print(f"Thread-{thread_number} done, saved final test at: {merged_path}")


if __name__ == '__main__':
    temperatures = [0.25]
    threads = []
    chromosomes_generator = ChromosomesGenerator(project_name='gson', source_code_path=r"C:\Users\liorb\PycharmProjects\EvoChat\benchmarks\gson\src\main\java\com\google\gson\JsonArray.java")
    for i, temp in enumerate(temperatures):
        t = threading.Thread(target=chromosomes_generator.threaded_generation, args=(i, temp))
        t.start()
        threads.append(t)

    for t in threads:
        t.join()

    print("All threads finished.")
    final_chromosomes = chromosomes_generator.chromosomes
    for c in final_chromosomes:
        print(c)