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
import random
import time

class ChromosomesGenerator:
    def __init__(self, source_code_path, project_name):
        self.source_code_path = source_code_path
        self.source_code_string = read_java_file_as_string(self.source_code_path)
        filename = os.path.basename(self.source_code_path)  # 'JsonArray.java'
        self.class_name = os.path.splitext(filename)[0]
        self.project_name = project_name
        self.chromosomes = []
        self.lock = threading.Lock()
        self.crossover_probability = 0.8

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
            f'C:\\Users\\liorb\\PycharmProjects\\EvoChat\\results\\unit_tests\\{self.project_name}\\{self.class_name}\\{str(thread_number)}\\javafiles',
            'JsonArray',
            'gson',
            thread_id=thread_number
        )
        jcc.generate_coverage_report()
        coverage_metrics, missed_branches = jcc.parse_jacoco_xml(
            rf'C:\Users\liorb\PycharmProjects\EvoChat\results\unit_tests\{self.project_name}\{self.class_name}\{str(thread_number)}\classfiles\coverage.xml'
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
        base_dir = rf'C:\Users\liorb\PycharmProjects\EvoChat\results\unit_tests\{self.project_name}\{self.class_name}\{str(thread_number)}\javafiles'
        base_cls_dir = rf'C:\Users\liorb\PycharmProjects\EvoChat\results\unit_tests\{self.project_name}\{self.class_name}\{str(thread_number)}\classfiles'
        test1_path = fr'{base_dir}\{self.class_name}Test.java'
        test2_path = fr'{base_dir}\{self.class_name}EnhancedTest.java'

        first_unit_test = read_java_file_as_string(test1_path)
        enhanced_unit_test = read_java_file_as_string(test2_path)
        final_unit_test = merge_java_unit_tests(first_unit_test, enhanced_unit_test)

        # Cleanup
        delete_file(test1_path)
        delete_file(test2_path)
        delete_file(fr'{base_cls_dir}')

        # Save final merged test
        merged_path = fr'{base_dir}\{self.class_name}Test.java'
        save_test_suite(final_unit_test, merged_path)
        chromosome_path = rf'C:\Users\liorb\PycharmProjects\EvoChat\results\unit_tests\{self.project_name}\{self.class_name}\{str(thread_number)}\javafiles'
        with self.lock:
            chromosome = Chromosome(path=chromosome_path, thread_id=thread_number)
            chromosome.compute_fitness()

        with self.lock:
            self.chromosomes.append(chromosome)
        print(f"Thread-{thread_number} done, saved final test at: {merged_path}")


    def select_two_parents(self):

        # Step 1: Sort chromosomes by fitness (higher is better)
        sorted_population = sorted(self.chromosomes, key=lambda c: c.fitness_score, reverse=True)

        # Step 2: Assign ranks (best = rank 0)
        N = len(sorted_population)
        ranks = list(range(N))

        # Step 3: Convert ranks to selection probabilities (better rank = higher weight)
        weights = [N - rank for rank in ranks]  # [N, N-1, ..., 1]
        total = sum(weights)
        probabilities = [w / total for w in weights]

        # Step 4: Select two parents using weighted sampling
        parents = random.choices(sorted_population, weights=probabilities, k=2)
        return parents[0], parents[1]

    def save_chromosome_code(self, iteration, offspring1_code, offspring2_code):
        base_path = os.path.join("results", "unit_tests", self.project_name, self.class_name,"offsprings", str(iteration))
        save_test_suite(offspring1_code,
                        os.path.join(base_path, "offspring1", "javafiles", f"{self.class_name}Test.java"))
        save_test_suite(self.source_code_string,
                        os.path.join(base_path, "offspring1", "javafiles", f"{self.class_name}.java"))
        compile_code_from_path(os.path.join(base_path, "offspring1", "javafiles", f"{self.class_name}.java"))
        compile_code_from_path(os.path.join(base_path, "offspring1", "javafiles", f"{self.class_name}Test.java"))
        save_test_suite(offspring2_code,
                        os.path.join(base_path, "offspring2", "javafiles", f"{self.class_name}Test.java"))
        save_test_suite(self.source_code_string,
                        os.path.join(base_path, "offspring2", "javafiles", f"{self.class_name}.java"))
        compile_code_from_path(os.path.join(base_path, "offspring2", "javafiles", f"{self.class_name}.java"))
        compile_code_from_path(os.path.join(base_path, "offspring2", "javafiles", f"{self.class_name}Test.java"))

    def create_chromosome(self, base_path):
        return Chromosome(path=base_path)

    def compute_gen_fitness(self):
        for chromosome in self.chromosomes:
            chromosome.compute_fitness()

    def evolution_generation(self, max_time=180):
        self.compute_gen_fitness()
        elites = list([])
        start_time = time.time()
        iterations = 0
        while time.time() - start_time < max_time:
            while len(elites) < len(self.chromosomes):
                parent1, parent2 = self.select_two_parents()
                if random.random() < self.crossover_probability:
                    offspring1_code, offspring2_code = parent1.crossover(parent2)
                else:
                    offspring1_code, offspring2_code = read_java_file_as_string(parent1.test_file_path), read_java_file_as_string(parent2.test_file_path)
                self.save_chromosome_code(iterations, offspring1_code, offspring2_code)
                base_path = os.path.join("results", "unit_tests", self.project_name, self.class_name,"offsprings", str(iterations))
                offspring1 = self.create_chromosome(os.path.join(base_path, "offspring1", "javafiles"))
                offspring2 = self.create_chromosome(os.path.join(base_path, "offspring2", "javafiles"))
                offspring1.compute_fitness()
                offspring2.compute_fitness()
                #TODO: Apply mutation to the offsprings here!
                """
                PUT MUTATION HERE
                """
                best_parent_fitness = max(parent1.fitness_score, parent2.fitness_score)
                best_offspring_fitness = max(offspring1.fitness_score, offspring2.fitness_score)

                parents_code_length = parent1.code_length + parent2.code_length
                offspring_code_length = offspring1.code_length + offspring2.code_length
                tb = max(self.chromosomes, key=lambda c: c.fitness_score)
                if best_offspring_fitness > best_parent_fitness or (best_offspring_fitness == best_parent_fitness and offspring_code_length <= parents_code_length):
                    offsprings = [offspring1, offspring2]
                    for offspring in offsprings:
                        if offspring.code_length <= 2 * tb.code_length:
                            elites.append(offspring)
                        else:
                            elites.append(random.choice([parent1, parent2]))
                else:
                    elites.append(parent1)
                    elites.append(parent2)
                iterations += 1

            self.chromosomes = elites + self.chromosomes
        return max(self.chromosomes, key=lambda c: c.fitness_score)

    def generate_final_unit_test(self,n_chromosomes=30, max_time=180):
        temperatures = [random.random() for _ in range(n_chromosomes)]
        threads = []

        for i, temp in enumerate(temperatures):
            i = i + 1
            t = threading.Thread(target=self.threaded_generation, args=(i, temp))
            t.start()
            threads.append(t)

        for t in threads:
            t.join()

        print("All threads finished.")

        final = chromosomes_generator.evolution_generation(max_time=max_time)
        print(final)
        #TODO: Delete all unit tests but this one.

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