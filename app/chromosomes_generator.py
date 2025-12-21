from config.config import *
from llm_agents.unit_test_generator import UnitTestGenerator
from llm_agents.coverage_enhancement_agent import CoverageEnhancementAgent
from llm_agents.plateau_escape_agent import PlateauEscapeAgent
from utils.function_utils import extract_project_name
from utils.java_executor import JavaExecutor
from copy import deepcopy
from utils.JavaCodeCoverage.Jacoco import JavaCodeCoverage
from utils.function_utils import *
# from sbst_agents.mutation_assertion_generator import MutationAssertionGenerator
from llm_agents.mutation_assertion_generation_agent import MutationAssertionGenerator
from utils.programmatic_mutator import ProgrammaticMutator
import threading
from app.chromosome import Chromosome
import random
import time
import asyncio
from concurrent.futures import ThreadPoolExecutor, as_completed


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

    async def threaded_generation(self,thread_number, temperature):
        try:
            print(f"Thread-{thread_number} starting with temperature={temperature}")

            unit_test_generator = UnitTestGenerator(api_key=API_KEY, model=MODEL, temperature=temperature)
            java_file_path = self.source_code_path
            project_id = self.project_name

            # Generation + Repair loop (initial)
            await unit_test_generator.generation_repair_loop(java_file_path=java_file_path, project_id=project_id,
                                                       thread_number=thread_number)

            # Coverage - Use cross-platform paths
            results_base = os.path.join(os.getcwd(), 'results', 'unit_tests', self.project_name, self.class_name, str(thread_number))
            javafiles_dir = os.path.join(results_base, 'javafiles')
            classfiles_dir = os.path.join(results_base, 'classfiles')
            
            jcc = JavaCodeCoverage(
                javafiles_dir,
                self.class_name,
                self.project_name,
                thread_id=thread_number
            )
            jcc.generate_coverage_report()
            coverage_xml_path = os.path.join(classfiles_dir, 'coverage.xml')
            coverage_metrics, missed_branches = jcc.parse_jacoco_xml(coverage_xml_path)

            # Enhancements
            test_enhancements = CoverageEnhancementAgent(
                api_key=API_KEY,
                model=MODEL,
                temperature=temperature,
                java_file_path=java_file_path
            )
            await test_enhancements.generation_repair_loop(coverage_metrics, missed_branches, thread_number=thread_number)
            # Paths with thread-specific test names
            base_dir = javafiles_dir
            base_cls_dir = classfiles_dir
            test1_path = os.path.join(base_dir, f'{self.class_name}Test.java')
            test2_path = os.path.join(base_dir, f'{self.class_name}EnhancedTest.java')

            first_unit_test = read_java_file_as_string(test1_path)
            enhanced_unit_test = read_java_file_as_string(test2_path)
            final_unit_test = merge_java_unit_tests(first_unit_test, enhanced_unit_test, f'{self.class_name}Test')
            # Cleanup
            delete_file(test1_path)
            delete_file(test2_path)
            delete_file(base_cls_dir)

            # Save final merged test - use cross-platform path
            merged_path = os.path.join(base_dir, f'{self.class_name}Test.java')
            save_test_suite(final_unit_test, merged_path)
            chromosome_path = javafiles_dir  # Use the already-defined cross-platform path
            with self.lock:
                chromosome = Chromosome(path=chromosome_path, thread_id=thread_number)
                chromosome.compute_fitness()

            with self.lock:
                self.chromosomes.append(chromosome)
            print(f"Agent-{thread_number} done, saved final test at: {merged_path}")
        except Exception as e:
            print(f"FAILED TO GENERATE CHROMOSOME! {e}")



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
        while parents[0] == parents[1]:
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

    async def _inject_llm_tests(self, best_chromosome):
        """
        CodaMosa-style LLM injection: Generate targeted test methods and inject them
        into the best chromosome's test suite to escape coverage plateaus.
        
        Args:
            best_chromosome: The chromosome with the highest fitness score
        """
        # Initialize the PlateauEscapeAgent
        escape_agent = PlateauEscapeAgent(api_key=API_KEY, model=MODEL)
        
        # Get coverage gaps from the best chromosome
        # We need to regenerate coverage report to get missed branches
        jcc = JavaCodeCoverage(
            os.path.dirname(best_chromosome.test_file_path),
            self.class_name,
            self.project_name,
            thread_id=best_chromosome.thread_id
        )
        
        # Parse coverage to get missed branches
        classfiles_dir = os.path.join(os.path.dirname(best_chromosome.path), "classfiles")
        coverage_xml_path = os.path.join(classfiles_dir, 'coverage.xml')
        
        # Check if coverage.xml exists, if not regenerate
        if not os.path.exists(coverage_xml_path):
            jcc.generate_coverage_report()
        
        coverage_metrics, missed_branches = jcc.parse_jacoco_xml(coverage_xml_path)
        
        # Format missed branches as string for the LLM
        missed_branches_str = "\n".join([
            f"Line {line_num}: {code}" 
            for line_num, code in missed_branches.items()
        ])
        
        if not missed_branches_str:
            missed_branches_str = "No specific missed branches identified. Focus on improving overall coverage."
        
        print(f"  Coverage gaps identified: {len(missed_branches)} missed branches")
        
        # Generate targeted test methods
        new_methods = await escape_agent.generate_targeted_tests(
            test_file_path=best_chromosome.test_file_path,
            source_code_path=self.source_code_path,
            branch_coverage=best_chromosome.branch_coverage,
            line_coverage=best_chromosome.line_coverage,
            mutation_score=best_chromosome.mutation_score,
            missed_branches=missed_branches_str,
            methods_per_agent=2  # Each agent generates 2 methods
        )
        
        if not new_methods:
            print("  No test methods generated by injection agents.")
            return
        
        # Get existing method names to avoid collisions
        existing_names = extract_test_method_names(
            read_java_file_as_string(best_chromosome.test_file_path)
        )
        
        # Deduplicate methods across agents
        unique_methods = escape_agent.deduplicate_methods(new_methods, existing_names)
        
        print(f"  Injecting {len(unique_methods)} unique test methods into best chromosome...")
        
        # Inject methods into the test file
        test_file_code = read_java_file_as_string(best_chromosome.test_file_path)
        modified_code = inject_test_methods(test_file_code, unique_methods, existing_names)
        
        # Save the modified test file
        save_test_suite(modified_code, best_chromosome.test_file_path)
        
        # Recompile the test file
        executor = JavaExecutor(best_chromosome.test_file_path)
        success, output = executor.compile_java()
        
        if not success:
            print(f"  WARNING: Compilation failed after injection. Attempting to fix...")
            # Try to fix by removing failing tests
            current_code = read_java_file_as_string(best_chromosome.test_file_path)
            fixed_code = remove_junit_tests(current_code, output)
            save_test_suite(fixed_code, best_chromosome.test_file_path)
            
            # Try compiling again
            success, output = executor.compile_java()
            if not success:
                print(f"  ERROR: Could not fix compilation errors. Reverting to original.")
                save_test_suite(test_file_code, best_chromosome.test_file_path)
                return
        
        # Recompute fitness for the modified chromosome
        print(f"  Recomputing fitness for injected chromosome...")
        best_chromosome.code_length = len(read_java_file_as_string(best_chromosome.test_file_path))
        best_chromosome.compute_fitness()
        
        print(f"  Injection successful! New fitness: {best_chromosome.fitness_score:.3f}")
        print(f"  Branch: {best_chromosome.branch_coverage:.1f}%, Line: {best_chromosome.line_coverage:.1f}%, Mutation: {best_chromosome.mutation_score:.1f}%")

    async def evolution_generation(self, max_generations=25):
        # self.compute_gen_fitness()
        offspring_pairs_generated = 0
        best_initial = max(self.chromosomes, key=lambda c: c.fitness_score)
        print(f"\n{'='*60}")
        print(f"Starting Evolution: {max_generations} offspring pairs to generate")
        print(f"Initial population size: {len(self.chromosomes)}")
        print(f"Best initial fitness: {best_initial.fitness_score:.3f}")
        if LLM_INJECTION_ENABLED:
            print(f"LLM Injection: ENABLED (threshold={STAGNATION_THRESHOLD}, max={MAX_INJECTIONS}, agents=5)")
        else:
            print(f"LLM Injection: DISABLED")
        print(f"{'='*60}\n")
        
        # CodaMosa-style stagnation tracking
        stagnation_counter = 0
        last_best_fitness = best_initial.fitness_score
        injection_count = 0
        
        while offspring_pairs_generated < max_generations:
            if max(self.chromosomes, key=lambda c: c.fitness_score).fitness_score == float(100):
                print(f"Perfect fitness (100.0) achieved after {offspring_pairs_generated} offspring pairs!")
                return max(self.chromosomes, key=lambda c: c.fitness_score)
            
            parent1, parent2 = self.select_two_parents()
            
            try:
                if random.random() < self.crossover_probability:
                    offspring1_code, offspring2_code = parent1.crossover(parent2)
                else:
                    offspring1_code, offspring2_code = read_java_file_as_string(parent1.test_file_path), read_java_file_as_string(parent2.test_file_path)
                self.save_chromosome_code(offspring_pairs_generated, offspring1_code, offspring2_code)
                base_path = os.path.join("results", "unit_tests", self.project_name, self.class_name,"offsprings", str(offspring_pairs_generated))
                offspring1 = self.create_chromosome(os.path.join(base_path, "offspring1", "javafiles"))
                offspring2 = self.create_chromosome(os.path.join(base_path, "offspring2", "javafiles"))

                # Select mutation strategy based on configuration
                if MUTATION_STRATEGY == 'llm':
                    off_1_mag = MutationAssertionGenerator(
                        api_key=API_KEY,
                        model=MODEL,
                        temperature=TEMPERATURE,
                        unit_test_path=os.path.join(base_path, "offspring1", "javafiles", f"{self.class_name}Test.java"),
                        source_code_path=os.path.join(base_path, "offspring1", "javafiles", f"{self.class_name}.java")
                    )
                    off_2_mag = MutationAssertionGenerator(
                        api_key=API_KEY,
                        model=MODEL,
                        temperature=TEMPERATURE,
                        unit_test_path=os.path.join(base_path, "offspring2", "javafiles", f"{self.class_name}Test.java"),
                        source_code_path=os.path.join(base_path, "offspring2", "javafiles", f"{self.class_name}.java")
                    )
                elif MUTATION_STRATEGY == 'programmatic':
                    off_1_mag = ProgrammaticMutator(
                        unit_test_path=os.path.join(base_path, "offspring1", "javafiles", f"{self.class_name}Test.java"),
                        source_code_path=os.path.join(base_path, "offspring1", "javafiles", f"{self.class_name}.java"),
                        mutation_probability=PROGRAMMATIC_MUTATION_PROBABILITY
                    )
                    off_2_mag = ProgrammaticMutator(
                        unit_test_path=os.path.join(base_path, "offspring2", "javafiles", f"{self.class_name}Test.java"),
                        source_code_path=os.path.join(base_path, "offspring2", "javafiles", f"{self.class_name}.java"),
                        mutation_probability=PROGRAMMATIC_MUTATION_PROBABILITY
                    )
                else:
                    raise ValueError(f"Unknown MUTATION_STRATEGY: '{MUTATION_STRATEGY}'. Expected 'llm' or 'programmatic'.")
                
                # Apply mutations (both mutators use the same interface)
                await asyncio.gather(off_2_mag.assertion_generation(), off_1_mag.assertion_generation())
                
                # Parallel fitness computation (3x faster than sequential)
                await asyncio.gather(
                    asyncio.to_thread(offspring1.compute_fitness),
                    asyncio.to_thread(offspring2.compute_fitness)
                )
                best_parent_fitness = max(parent1.fitness_score, parent2.fitness_score)
                best_offspring_fitness = max(offspring1.fitness_score, offspring2.fitness_score)

                parents_code_length = parent1.code_length + parent2.code_length
                offspring_code_length = offspring1.code_length + offspring2.code_length
                tb = max(self.chromosomes, key=lambda c: c.fitness_score)
                if best_offspring_fitness > best_parent_fitness or (best_offspring_fitness == best_parent_fitness and offspring_code_length <= parents_code_length):
                    offsprings = [offspring1, offspring2]
                    for offspring in offsprings:
                        if offspring.code_length <= 2 * tb.code_length:
                            self.chromosomes.append(offspring)
                            if offspring.fitness_score == 100:
                                return offspring
                        else:
                            self.chromosomes.append(random.choice([parent1, parent2]))
                else:
                    self.chromosomes.append(parent1)
                    self.chromosomes.append(parent2)
                
                offspring_pairs_generated += 1
                
                # CodaMosa-style stagnation detection and LLM injection
                current_best = max(self.chromosomes, key=lambda c: c.fitness_score)
                fitness_improvement = current_best.fitness_score - last_best_fitness
                
                if fitness_improvement >= MIN_FITNESS_IMPROVEMENT:
                    # Progress made - reset stagnation counter
                    stagnation_counter = 0
                    last_best_fitness = current_best.fitness_score
                else:
                    # No significant improvement - increment stagnation counter
                    stagnation_counter += 1
                
                # Check if we should trigger LLM injection
                if (LLM_INJECTION_ENABLED and 
                    stagnation_counter >= STAGNATION_THRESHOLD and 
                    injection_count < MAX_INJECTIONS):
                    
                    print(f"\n{'='*60}")
                    print(f"STAGNATION DETECTED after {stagnation_counter} iterations without improvement!")
                    print(f"Triggering CodaMosa-style LLM injection #{injection_count + 1}/{MAX_INJECTIONS}...")
                    print(f"{'='*60}")
                    
                    try:
                        await self._inject_llm_tests(current_best)
                        stagnation_counter = 0  # Reset counter after injection
                        injection_count += 1
                        # Update best fitness after injection
                        new_best = max(self.chromosomes, key=lambda c: c.fitness_score)
                        last_best_fitness = new_best.fitness_score
                        print(f"Injection complete. New best fitness: {new_best.fitness_score:.3f}")
                    except Exception as inject_error:
                        print(f"LLM injection failed: {inject_error}")
                        stagnation_counter = 0  # Reset to avoid repeated failures
                
                # Print progress every 5 offspring pairs
                if offspring_pairs_generated % 5 == 0:
                    best_current = max(self.chromosomes, key=lambda c: c.fitness_score)
                    avg_fitness = sum(c.fitness_score for c in self.chromosomes) / len(self.chromosomes)
                    stagnation_info = f", stagnation={stagnation_counter}" if LLM_INJECTION_ENABLED else ""
                    print(f"Offspring pairs: {offspring_pairs_generated}/{max_generations} - Best: {best_current.fitness_score:.3f}, Avg: {avg_fitness:.3f}, Pop size: {len(self.chromosomes)}{stagnation_info}")
                
            except Exception as e:
                print(f"Error generating offspring pair {offspring_pairs_generated}: {e}")
                offspring_pairs_generated += 1  # Count failed attempts too
        
        # Ensure best initial is preserved
        if best_initial not in self.chromosomes:
            self.chromosomes.append(best_initial)
        
        final_best = max(self.chromosomes, key=lambda c: c.fitness_score)
        print(f"\n{'='*60}")
        print(f"Evolution Complete!")
        print(f"Generated {offspring_pairs_generated} offspring pairs")
        print(f"Final population size: {len(self.chromosomes)}")
        print(f"Final best fitness: {final_best.fitness_score:.3f}")
        print(f"{'='*60}\n")
        return final_best

    async def generate_final_unit_test(self, n_chromosomes=30, max_generations=25):
        base_temps = [0.3, 0.4, 0.5, 0.6, 0.8]

        # Repeat each temperature evenly
        repeats_per_temp = n_chromosomes // len(base_temps)
        temperatures = base_temps * repeats_per_temp
        tasks = [
            self.threaded_generation(i + 1, temp)
            for i, temp in enumerate(temperatures)
        ]
        await asyncio.gather(*tasks)
        print("All async agents finished.")
        print(self.chromosomes)
        final = await self.evolution_generation(max_generations=max_generations)

        for chromosome in self.chromosomes:
            print(chromosome)
        print(f"BEST CHROMOSOME: {final}")
        print(
            f"Line coverage: {final.line_coverage}, Branch coverage: {final.branch_coverage}, Mutation Score: {final.mutation_score}, Test Strength: {final.tests_strength}")

        # Save only the best chromosome's test suite and clean up everything else
        self._save_best_and_cleanup(final)

    def _save_best_and_cleanup(self, best_chromosome):
        """
        Save the best chromosome's test suite to the final location and delete all other files.
        Final location: results/unit_tests/{project_name}/{class_name}/{class_name}Test.java
        """
        import shutil
        
        # Define final directory path
        final_dir = os.path.join("results", "unit_tests", self.project_name, self.class_name)
        final_test_path = os.path.join(final_dir, f"{self.class_name}Test.java")
        
        # Read the best chromosome's test suite - handle missing file
        best_test_content = read_java_file_as_string(best_chromosome.test_file_path)
        if best_test_content is None:
            # Try to find the test file by searching for it
            search_paths = [
                os.path.join(os.path.dirname(best_chromosome.path), "javafiles", f"{self.class_name}Test.java"),
                os.path.join(best_chromosome.path, f"{self.class_name}Test.java"),
            ]
            for search_path in search_paths:
                best_test_content = read_java_file_as_string(search_path)
                if best_test_content is not None:
                    print(f"✓ Found test file at alternate location: {search_path}")
                    break
            
            if best_test_content is None:
                raise FileNotFoundError(
                    f"Could not find best chromosome test file at {best_chromosome.test_file_path} "
                    f"or any alternate locations. Cannot save final test suite."
                )
        
        # Get the source code path to save alongside (optional but useful)
        source_code_content = self.source_code_string
        final_source_path = os.path.join(final_dir, f"{self.class_name}.java")
        
        # Create a temporary backup directory to store the best test before cleanup
        temp_backup_dir = os.path.join("results", "unit_tests", f".temp_backup_{self.class_name}")
        os.makedirs(temp_backup_dir, exist_ok=True)
        temp_test_backup = os.path.join(temp_backup_dir, f"{self.class_name}Test.java")
        temp_source_backup = os.path.join(temp_backup_dir, f"{self.class_name}.java")
        
        # Save to temporary backup
        save_test_suite(best_test_content, temp_test_backup)
        save_test_suite(source_code_content, temp_source_backup)
        
        print(f"\n{'='*60}")
        print(f"Saving best test suite and cleaning up...")
        print(f"{'='*60}")
        
        # Delete the entire results directory for this project and class
        project_results_dir = os.path.join("results", "unit_tests", self.project_name, self.class_name)
        if os.path.exists(project_results_dir):
            from config.config import PRESERVE_INITIAL_POOL
            if not PRESERVE_INITIAL_POOL:
                shutil.rmtree(project_results_dir)
                print(f"✓ Cleaned up temporary files from: {project_results_dir}")
            else:
                # Preserve initial pool for diversity analysis
                offsprings_dir = os.path.join(project_results_dir, "offsprings")
                if os.path.exists(offsprings_dir):
                    shutil.rmtree(offsprings_dir)
                print(f"ℹ  Preserved initial pool at: {project_results_dir}")
            print(f"✓ Cleaned up temporary files from: {project_results_dir}")
        
        # Recreate the final directory
        os.makedirs(final_dir, exist_ok=True)
        
        # Move the best test from backup to final location
        shutil.copy(temp_test_backup, final_test_path)
        shutil.copy(temp_source_backup, final_source_path)
        
        # Remove temporary backup
        shutil.rmtree(temp_backup_dir)
        
        print(f"✓ Saved best test suite to: {final_test_path}")
        print(f"✓ Saved source code to: {final_source_path}")
        print(f"\n{'='*60}")
        print(f"🎉 FINAL TEST SUITE READY!")
        print(f"{'='*60}")
        print(f"Location: {os.path.abspath(final_test_path)}")
        print(f"Fitness Score: {best_chromosome.fitness_score:.3f}")
        print(f"Branch Coverage: {best_chromosome.branch_coverage:.2f}%")
        print(f"Line Coverage: {best_chromosome.line_coverage:.2f}%")
        print(f"Mutation Score: {best_chromosome.mutation_score:.2f}%")
        print(f"Test Strength: {best_chromosome.tests_strength:.2f}%")
        print(f"{'='*60}\n")

if __name__ == '__main__':
    temperatures = [0.25]
    threads = []
    chromosomes_generator = ChromosomesGenerator(project_name='gson', source_code_path=r"benchmarks/gson/src/main/java/com/google/gson/JsonArray.java")
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