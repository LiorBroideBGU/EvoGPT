from config.config_loader import get_config
from llm_agents.unit_test_generator import UnitTestGenerator
from llm_agents.coverage_enhancement_agent import CoverageEnhancementAgent
from llm_agents.plateau_escape_agent import PlateauEscapeAgent
from utils.java_executor import JavaExecutor
from utils.JavaCodeCoverage.Jacoco import JavaCodeCoverage
from utils.function_utils import *
from llm_agents.mutation_assertion_generation_agent import MutationAssertionGenerator
from utils.programmatic_mutator import ProgrammaticMutator
import threading
from app.chromosome import Chromosome
import random
import time
from pathlib import Path
import logging
import asyncio



class ChromosomesGenerator:
    def __init__(self, source_code_path: Path, project_name: str):
        self.logger = logging.getLogger(__name__)
        self.source_code_path = source_code_path
        self.source_code_string = read_java_file_as_string(self.source_code_path)
        filename = os.path.basename(self.source_code_path)  # 'JsonArray.java'
        self.class_name = os.path.splitext(filename)[0]
        self.project_name = project_name
        self.chromosomes = []
        self.lock = threading.Lock()
        self.crossover_probability = 0.8

    async def threaded_generation(self, thread_number: int, temperature: float, max_retries: int = 3, output_path: Path = Path.cwd()):
        import sys as _sys

        for attempt in range(1, max_retries + 1):
            try:
                self.logger.debug(f"Thread-{thread_number} starting (attempt {attempt}/{max_retries}) with temperature={temperature}")
                cfg = get_config()
                unit_test_generator = UnitTestGenerator(api_key=cfg.API_KEY, model=cfg.MODEL, temperature=temperature)
                # Generation + Repair loop (initial)
                generated_running_test = await unit_test_generator.generation_repair_loop(
                    java_file_path=self.source_code_path, project_id=self.project_name, thread_number=thread_number, output_path=output_path)

                if not generated_running_test:
                    raise RuntimeError("Failed to generate a valid test after repair loop.")

                # Coverage
                self.logger.debug(f"Successfully generated a test for Thread-{thread_number}, verifying code coverage...")
                results_base = output_path / 'unit_tests' / self.project_name / self.class_name / str(thread_number)
                javafiles_dir = results_base / 'javafiles'
                classfiles_dir = results_base / 'classfiles'
                jcc = JavaCodeCoverage(java_files_dir=javafiles_dir, test_class=self.class_name, project_name=self.project_name, thread_id=thread_number)
                coverage_ok = jcc.generate_coverage_report(output_path=output_path)
                coverage_xml_path = classfiles_dir / 'coverage.xml'

                generated_test_path = javafiles_dir / f'{self.class_name}Test.java'
                if coverage_ok and coverage_xml_path.exists():
                    coverage_metrics, missed_branches = jcc.parse_jacoco_xml(coverage_xml_path)

                    # Enhancements
                    test_enhancements = CoverageEnhancementAgent(
                        api_key=cfg.API_KEY, model=cfg.MODEL, temperature=temperature, java_file_path=self.source_code_path)
                    await test_enhancements.generation_repair_loop(coverage_metrics, missed_branches, thread_number=thread_number, output_path=output_path)

                    first_unit_test = read_java_file_as_string(generated_test_path)
                    enhanced_unit_test = read_java_file_as_string(javafiles_dir / f'{self.class_name}EnhancedTest.java')
                    final_unit_test = merge_java_unit_tests(first_unit_test, enhanced_unit_test, f'{self.class_name}Test')
                else:
                    self.logger.debug(f"Thread-{thread_number} coverage report failed; skipping enhancement, using base test")
                    final_unit_test = read_java_file_as_string(generated_test_path)

                # Cleanup
                delete_paths([generated_test_path, javafiles_dir / f'{self.class_name}EnhancedTest.java', classfiles_dir])

                # Save final merged test
                save_code(final_unit_test, generated_test_path)
                chromosome_path = javafiles_dir
                with self.lock:
                    chromosome = Chromosome(path=chromosome_path, thread_id=thread_number)
                    chromosome.compute_fitness(output_path=output_path)

                with self.lock:
                    self.chromosomes.append(chromosome)
                
                self.logger.debug(f"Agent-{thread_number} done, saved final test at: {generated_test_path}")
                return

            except Exception as e:
                print(f"[Thread-{thread_number}] Attempt {attempt}/{max_retries} failed: {e}", file=_sys.stderr)
                if attempt >= max_retries:
                    print(f"[Thread-{thread_number}] All {max_retries} attempts exhausted for {self.class_name}", file=_sys.stderr)



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

    def save_chromosome_code(self, iteration: int, offspring1_code: str, offspring2_code: str, results_dir: str):
        base_path = Path(results_dir) / "unit_tests" / self.project_name / self.class_name / "offsprings" / str(iteration)
        save_code(offspring1_code,
                  base_path / "offspring1" / "javafiles" / f"{self.class_name}Test.java")
        save_code(self.source_code_string,
                  base_path / "offspring1" / "javafiles" / f"{self.class_name}.java")
        compile_code_from_path(base_path / "offspring1" / "javafiles" / f"{self.class_name}.java")
        compile_code_from_path(base_path / "offspring1" / "javafiles" / f"{self.class_name}Test.java")
        save_code(offspring2_code,
                  base_path / "offspring2" / "javafiles" / f"{self.class_name}Test.java")
        save_code(self.source_code_string,
                  base_path / "offspring2" / "javafiles" / f"{self.class_name}.java")
        compile_code_from_path(base_path / "offspring2" / "javafiles" / f"{self.class_name}.java")
        compile_code_from_path(base_path / "offspring2" / "javafiles" / f"{self.class_name}Test.java")

    def create_chromosome(self, base_path: Path):
        return Chromosome(path=base_path)

    def compute_gen_fitness(self):
        for chromosome in self.chromosomes:
            chromosome.compute_fitness()

    async def _inject_llm_tests(self, best_chromosome: Chromosome, output_path: Path):
        """
        CodaMosa-style LLM injection: Generate targeted test methods and inject them
        into the best chromosome's test suite to escape coverage plateaus.
        
        Args:
            best_chromosome: The chromosome with the highest fitness score
            output_path: The path to the output directory
        """
        # Initialize the PlateauEscapeAgent
        cfg = get_config()
        escape_agent = PlateauEscapeAgent(api_key=cfg.API_KEY, model=cfg.MODEL)
        
        # Get coverage gaps from the best chromosome
        # We need to regenerate coverage report to get missed branches
        jcc = JavaCodeCoverage(
            best_chromosome.test_file_path.parent,
            self.class_name,
            self.project_name,
            thread_id=best_chromosome.thread_id
        )
        
        # Parse coverage to get missed branches
        coverage_xml_path = best_chromosome.path.parent / "classfiles" / 'coverage.xml'
        
        # Check if coverage.xml exists, if not regenerate
        if not coverage_xml_path.exists():
            jcc.generate_coverage_report(output_path=output_path)
        
        coverage_metrics, missed_branches = jcc.parse_jacoco_xml(xml_file=coverage_xml_path)
        self.logger.debug(f"Coverage metrics: {coverage_metrics}")
        
        # Format missed branches as string for the LLM
        missed_branches_str = "\n".join([
            f"Line {line_num}: {code}" 
            for line_num, code in missed_branches.items()
        ])
        
        if not missed_branches_str:
            missed_branches_str = "No specific missed branches identified. Focus on improving overall coverage."
        
        self.logger.debug(f"Coverage gaps identified: {len(missed_branches)} missed branches")
        
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
            self.logger.debug("No test methods generated by injection agents.")
            return
        
        # Get existing method names to avoid collisions
        existing_names = extract_test_method_names(
            read_java_file_as_string(best_chromosome.test_file_path)
        )
        
        # Deduplicate methods across agents
        unique_methods = escape_agent.deduplicate_methods(new_methods, existing_names)
        
        self.logger.debug(f"Injecting {len(unique_methods)} unique test methods into best chromosome...")
        
        # Inject methods into the test file
        test_file_code = read_java_file_as_string(best_chromosome.test_file_path)
        modified_code = inject_test_methods(test_file_code, unique_methods, existing_names)
        
        # Save the modified test file
        save_code(modified_code, best_chromosome.test_file_path)
        
        # Recompile the test file
        executor = JavaExecutor(best_chromosome.test_file_path)
        success, output = executor.compile_java()
        
        if not success:
            self.logger.warning(f"Compilation failed after injection. Attempting to fix...")
            # Try to fix by removing failing tests
            current_code = read_java_file_as_string(best_chromosome.test_file_path)
            fixed_code = remove_junit_tests(current_code, output)
            save_code(fixed_code, best_chromosome.test_file_path)
            
            # Try compiling again
            success, output = executor.compile_java()
            if not success:
                self.logger.error(f"Could not fix compilation errors. Reverting to original.")
                save_code(test_file_code, best_chromosome.test_file_path)
                return
        
        # Recompute fitness for the modified chromosome
        self.logger.debug(f"Recomputing fitness for injected chromosome...")
        best_chromosome.code_length = len(read_java_file_as_string(best_chromosome.test_file_path))
        best_chromosome.compute_fitness(output_path=output_path)
        
        self.logger.debug(f"Injection successful! New fitness: {best_chromosome.fitness_score:.3f}")
        self.logger.debug(f"Branch: {best_chromosome.branch_coverage:.1f}%, Line: {best_chromosome.line_coverage:.1f}%, Mutation: {best_chromosome.mutation_score:.1f}%")

    async def evolution_generation(self, max_generations=25, time_limit_seconds=None, output_path: Path = Path.cwd()):
        if not self.chromosomes:
            raise RuntimeError("No chromosomes were generated in the initial population. "
                               "All LLM agents failed to produce valid tests.")
        offspring_pairs_generated = 0
        best_initial = max(self.chromosomes, key=lambda c: c.fitness_score)
        evolution_start_time = time.time()
        self.logger.debug(f"\n{'='*60}")
        if time_limit_seconds:
            self.logger.debug(f"Starting Evolution: time limit {time_limit_seconds}s (max {max_generations} offspring pairs)")
        else:
            self.logger.debug(f"Starting Evolution: {max_generations} offspring pairs to generate")
        self.logger.debug(f"Initial population size: {len(self.chromosomes)}")
        self.logger.debug(f"Best initial fitness: {best_initial.fitness_score:.3f}")
        cfg = get_config()
        if cfg.LLM_INJECTION_ENABLED:
            self.logger.debug(f"LLM Injection: ENABLED (threshold={cfg.STAGNATION_THRESHOLD}, max={cfg.MAX_INJECTIONS}, agents=5)")
        else:
            self.logger.debug(f"LLM Injection: DISABLED")
        self.logger.debug(f"{'='*60}\n")
        
        # CodaMosa-style stagnation tracking
        stagnation_counter = 0
        last_best_fitness = best_initial.fitness_score
        injection_count = 0
        
        while offspring_pairs_generated < max_generations:
            if time_limit_seconds and (time.time() - evolution_start_time) >= time_limit_seconds:
                self.logger.debug(f"Time limit ({time_limit_seconds}s) reached after {offspring_pairs_generated} offspring pairs.")
                break
            if max(self.chromosomes, key=lambda c: c.fitness_score).fitness_score == float(100):
                self.logger.debug(f"Perfect fitness (100.0) achieved after {offspring_pairs_generated} offspring pairs!")
                return max(self.chromosomes, key=lambda c: c.fitness_score)
            
            parent1, parent2 = self.select_two_parents()
            
            try:
                if random.random() < self.crossover_probability:
                    offspring1_code, offspring2_code = parent1.crossover(parent2)
                else:
                    offspring1_code, offspring2_code = read_java_file_as_string(parent1.test_file_path), read_java_file_as_string(parent2.test_file_path)
                self.save_chromosome_code(offspring_pairs_generated, offspring1_code, offspring2_code, str(output_path))
                base_path = output_path / "unit_tests" / self.project_name / self.class_name / "offsprings" / str(offspring_pairs_generated)
                offspring1 = self.create_chromosome(base_path / "offspring1" / "javafiles")
                offspring2 = self.create_chromosome(base_path / "offspring2" / "javafiles")

                # Select mutation strategy based on configuration
                if cfg.MUTATION_STRATEGY == 'llm':
                    off_1_mag = MutationAssertionGenerator(
                        api_key=cfg.API_KEY,
                        model=cfg.MODEL,
                        temperature=cfg.TEMPERATURE,
                        unit_test_path=base_path / "offspring1" / "javafiles" / f"{self.class_name}Test.java",
                        source_code_path=base_path / "offspring1" / "javafiles" / f"{self.class_name}.java"
                    )
                    off_2_mag = MutationAssertionGenerator(
                        api_key=cfg.API_KEY,
                        model=cfg.MODEL,
                        temperature=cfg.TEMPERATURE,
                        unit_test_path=base_path / "offspring2" / "javafiles" / f"{self.class_name}Test.java",
                        source_code_path=base_path / "offspring2" / "javafiles" / f"{self.class_name}.java"
                    )
                elif cfg.MUTATION_STRATEGY == 'programmatic':
                    off_1_mag = ProgrammaticMutator(
                        unit_test_path=base_path/ "offspring1" / "javafiles" / f"{self.class_name}Test.java",
                        source_code_path=base_path/ "offspring1" / "javafiles" / f"{self.class_name}.java",
                        mutation_probability=cfg.PROGRAMMATIC_MUTATION_PROBABILITY
                    )
                    off_2_mag = ProgrammaticMutator(
                        unit_test_path=base_path/ "offspring2" / "javafiles" / f"{self.class_name}Test.java",
                        source_code_path=base_path/ "offspring2" / "javafiles" / f"{self.class_name}.java",
                        mutation_probability=cfg.PROGRAMMATIC_MUTATION_PROBABILITY
                    )
                else:
                    raise ValueError(f"Unknown MUTATION_STRATEGY: '{cfg.MUTATION_STRATEGY}'. Expected 'llm' or 'programmatic'.")
                
                # Apply mutations (both mutators use the same interface)
                await asyncio.gather(off_2_mag.assertion_generation(), off_1_mag.assertion_generation())
                
                # Parallel fitness computation (3x faster than sequential)
                await asyncio.gather(
                    asyncio.to_thread(offspring1.compute_fitness, output_path),
                    asyncio.to_thread(offspring2.compute_fitness, output_path)
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
                
                if fitness_improvement >= cfg.MIN_FITNESS_IMPROVEMENT:
                    # Progress made - reset stagnation counter
                    stagnation_counter = 0
                    last_best_fitness = current_best.fitness_score
                else:
                    # No significant improvement - increment stagnation counter
                    stagnation_counter += 1
                
                # Check if we should trigger LLM injection
                if (cfg.LLM_INJECTION_ENABLED and
                    stagnation_counter >= cfg.STAGNATION_THRESHOLD and
                    injection_count < cfg.MAX_INJECTIONS):
                    
                    self.logger.debug(f"\n{'='*60}")
                    self.logger.debug(f"STAGNATION DETECTED after {stagnation_counter} iterations without improvement!")
                    self.logger.debug(f"Triggering CodaMosa-style LLM injection #{injection_count + 1}/{cfg.MAX_INJECTIONS}...")
                    self.logger.debug(f"{'='*60}")
                    
                    try:
                        await self._inject_llm_tests(current_best, output_path)
                        stagnation_counter = 0  # Reset counter after injection
                        injection_count += 1
                        # Update best fitness after injection
                        new_best = max(self.chromosomes, key=lambda c: c.fitness_score)
                        last_best_fitness = new_best.fitness_score
                        self.logger.debug(f"Injection complete. New best fitness: {new_best.fitness_score:.3f}")
                    except Exception as inject_error:
                        self.logger.debug(f"LLM injection failed: {inject_error}")
                        stagnation_counter = 0  # Reset to avoid repeated failures
                
                # Print progress every 5 offspring pairs
                if offspring_pairs_generated % 5 == 0:
                    best_current = max(self.chromosomes, key=lambda c: c.fitness_score)
                    avg_fitness = sum(c.fitness_score for c in self.chromosomes) / len(self.chromosomes)
                    stagnation_info = f", stagnation={stagnation_counter}" if cfg.LLM_INJECTION_ENABLED else ""
                    self.logger.debug(f"Offspring pairs: {offspring_pairs_generated}/{max_generations} - Best: {best_current.fitness_score:.3f}, Avg: {avg_fitness:.3f}, Pop size: {len(self.chromosomes)}{stagnation_info}")
                
            except Exception as e:
                self.logger.debug(f"Error generating offspring pair {offspring_pairs_generated}: {e}")
                offspring_pairs_generated += 1  # Count failed attempts too
        
        # Ensure best initial is preserved
        if best_initial not in self.chromosomes:
            self.chromosomes.append(best_initial)
        
        final_best = max(self.chromosomes, key=lambda c: c.fitness_score)
        self.logger.debug(f"\n{'='*60}")
        self.logger.debug(f"Evolution Complete!")
        self.logger.debug(f"Generated {offspring_pairs_generated} offspring pairs")
        self.logger.debug(f"Final population size: {len(self.chromosomes)}")
        self.logger.debug(f"Final best fitness: {final_best.fitness_score:.3f}")
        self.logger.debug(f"{'='*60}\n")
        return final_best

    async def generate_final_unit_test(self, n_chromosomes=30, max_generations=25, time_limit_seconds=None, output_path: Path = Path.cwd()):
        base_temps = [0.3, 0.4, 0.5, 0.6, 0.8]

        # Cycle through base temperatures to fill n_chromosomes slots
        temperatures = [base_temps[i % len(base_temps)] for i in range(n_chromosomes)]
        tasks = [
            self.threaded_generation(i + 1, temp, output_path=output_path)
            for i, temp in enumerate(temperatures)
        ]
        await asyncio.gather(*tasks)
        self.logger.debug(f"All async agents finished. Population size: {len(self.chromosomes)}")
        final = await self.evolution_generation(max_generations=max_generations, time_limit_seconds=time_limit_seconds, output_path=output_path)

        self.logger.debug(f"BEST CHROMOSOME: {final}")
        self.logger.debug(
            f"Line coverage: {final.line_coverage}, Branch coverage: {final.branch_coverage}, Mutation Score: {final.mutation_score}, Test Strength: {final.tests_strength}")

        # Save only the best chromosome's test suite and clean up everything else
        self._save_best_and_cleanup(final, output_path)

    def _save_best_and_cleanup(self, best_chromosome, output_path: Path | None = None):
        """
        Save the best chromosome's test suite to the final location and delete all other files.
        Final location: {output_path}/unit_tests/{project_name}/{class_name}/{class_name}Test.java
        """
        import shutil
        cfg = get_config()
        effective_output = Path(output_path) if output_path is not None else (Path(cfg.results_dir) if cfg.results_dir else Path.cwd())
        # Define final directory path
        final_dir = effective_output / "unit_tests" / self.project_name / self.class_name
        final_test_path = final_dir / f"{self.class_name}Test.java"
        
        # Read the best chromosome's test suite - handle missing file
        best_test_content = read_java_file_as_string(best_chromosome.test_file_path)
        if best_test_content is None:
            # Try to find the test file by searching for it
            search_paths = [
                best_chromosome.path / "javafiles" / f"{self.class_name}Test.java",
                best_chromosome.path / f"{self.class_name}Test.java",
            ]
            for search_path in search_paths:
                best_test_content = read_java_file_as_string(search_path)
                if best_test_content is not None:
                    self.logger.debug(f"Found test file at alternate location: {search_path}")
                    break
            
            if best_test_content is None:
                raise FileNotFoundError(
                    f"Could not find best chromosome test file at {best_chromosome.test_file_path} "
                    f"or any alternate locations. Cannot save final test suite."
                )
        
        # Get the source code path to save alongside (optional but useful)
        source_code_content = self.source_code_string
        final_source_path = final_dir / f"{self.class_name}.java"
        
        # Create a temporary backup directory to store the best test before cleanup
        temp_backup_dir = effective_output / "unit_tests" / f".temp_backup_{self.class_name}"
        temp_backup_dir.mkdir(parents=True, exist_ok=True)
        temp_test_backup = temp_backup_dir / f"{self.class_name}Test.java"
        temp_source_backup = temp_backup_dir / f"{self.class_name}.java"
        
        # Save to temporary backup
        save_code(best_test_content, temp_test_backup)
        save_code(source_code_content, temp_source_backup)
        
        self.logger.debug(f"Saving best test suite and cleaning up...")
        
        # Delete the entire output directory for this project and class
        project_results_dir = effective_output / "unit_tests" / self.project_name / self.class_name
        if os.path.exists(project_results_dir):
            cfg = get_config()
            if not cfg.PRESERVE_INITIAL_POOL:
                shutil.rmtree(project_results_dir)
                self.logger.debug(f"Cleaned up temporary files from: {project_results_dir}")
            else:
                offsprings_dir = project_results_dir / "offsprings"
                if os.path.exists(offsprings_dir):
                    shutil.rmtree(offsprings_dir)
                self.logger.debug(f"Preserved initial pool at: {project_results_dir}")
        
        # Recreate the final directory
        final_dir.mkdir(parents=True, exist_ok=True)
        
        # Move the best test from backup to final location
        shutil.copy(temp_test_backup, final_test_path)
        shutil.copy(temp_source_backup, final_source_path)
        
        # Remove temporary backup
        shutil.rmtree(temp_backup_dir)
        
        self.logger.debug(f"Saved best test suite to: {final_test_path}")
        self.logger.debug(f"Fitness: {best_chromosome.fitness_score:.3f} "
                          f"(branch={best_chromosome.branch_coverage:.1f}%, "
                          f"line={best_chromosome.line_coverage:.1f}%, "
                          f"mutation={best_chromosome.mutation_score:.1f}%)")

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