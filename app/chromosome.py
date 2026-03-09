from utils.JavaCodeCoverage.Jacoco import JavaCodeCoverage
from utils.MutationScoreGenerator.PITest import PITestRunner
from utils.function_utils import *
import random
from config.config_loader import get_config
from pathlib import Path

def extract_package_from_path(java_file_path):
    """
    Extract package name from a Java file path.
    E.g. /path/to/src/main/java/org/apache/commons/cli/Option.java -> org.apache.commons.cli
    """
    # Find the portion after 'src/main/java/' or 'src/'
    normalized_path = java_file_path.replace('\\', '/')
    
    if '/src/main/java/' in normalized_path:
        package_path = normalized_path.split('/src/main/java/')[1]
    elif '/src/' in normalized_path:
        package_path = normalized_path.split('/src/')[1]
    else:
        # Fallback: assume last few directories are the package
        parts = normalized_path.split('/')
        package_path = '/'.join(parts[-4:-1]) if len(parts) > 4 else '/'.join(parts[:-1])
    
    # Remove the filename and convert path separators to dots
    package_parts = package_path.split('/')[:-1]  # Remove filename
    package_name = '.'.join(package_parts)
    
    return package_name

class Chromosome:
    def __init__(self, path: Path, thread_id: int = None):
        """
        Initialize a chromosome representing a unit test file.
        :param path: The folder where this unit test lives (e.g. .../JsonArray/3).
        """
        self.path = path
        path_parts = [part for part in self.path.parts if part]
        try:
            if thread_id is not None:
                # Path: .../gson/JsonArray/7/javafiles -> JsonArray is at -3
                self.java_file_name = path_parts[-3] if len(path_parts) >= 3 else path_parts[-2] if len(path_parts) >= 2 else path_parts[-1]
            else:
                # For offspring paths without thread_id
                self.java_file_name = path_parts[-5] if len(path_parts) >= 5 else path_parts[-3] if len(path_parts) >= 3 else path_parts[-1]
        
        except IndexError:
            # Fallback: try to extract from path
            print(f"Warning: Could not extract class name from path: {path}, parts: {path_parts}")
            self.java_file_name = "UnknownClass"
            
        self.thread_id = thread_id
        self.test_file_path = self._locate_test_file()
        self.code_length = len(read_java_file_as_string(self.test_file_path))
        self.branch_coverage = None
        self.line_coverage = None
        self.mutation_score = None
        self.tests_strength = None
        self.fitness_score = None  # To be computed
        print(f"CHROMOSE GENERATION {thread_id}")


    def _fix_runtime_errors(self):
        success, output = False, None
        executor = JavaExecutor(self.test_file_path)
        current_test_suite = read_java_file_as_string(self.test_file_path)
        for i in range(6):
            success, output = executor.run_java()
            if not success:
                current_test_suite = remove_junit_tests(current_test_suite, output)
                save_code(current_test_suite, self.test_file_path)
            else:
                break

    def _locate_test_file(self):
        """
        Locate the main test file inside the directory.
        This assumes there's only one test file in the directory.
        """
        for file in self.path.glob("*.java"):
            if "Test" in file.name:
                return file.absolute()
                
        raise FileNotFoundError(f"No Java test file found in {self.path}")

    def compute_fitness(self, output_path: Path):
        """
        Compute the code's metrics and fitness score for this chromosome.
        Based on line coverage, branch coverage and mutation score.
        :param output_path: The path to the output directory
        """
        self._fix_runtime_errors()

        cfg = get_config()
        # Extract package name dynamically from CLASS_PATH
        package_name = extract_package_from_path(cfg.CLASS_PATH)
        fully_qualified_class = f"{package_name}.{self.java_file_name}"

        java_files_dir = self.path
        jcc = JavaCodeCoverage(java_files_dir, self.java_file_name, cfg.PROJECT, self.thread_id)

        parent_dir = self.path.parent
        mutation_scorer = PITestRunner(
            project_name=cfg.PROJECT,
            class_name=fully_qualified_class,  # fully qualified class name (e.g. org.apache.commons.cli.Option)
            classfiles_dir=parent_dir / "classfiles",
            source_dir=java_files_dir,
            report_dir=parent_dir / "pitest_report",
        )
        self.branch_coverage, self.line_coverage = jcc.get_average_coverage(thread_number=self.thread_id, output_path=output_path)
        self.mutation_score, self.tests_strength = mutation_scorer.run(self.java_file_name + 'Test')
        
        self.fitness_score = 0.3 * self.branch_coverage + 0.2 * self.line_coverage + 0.5 * self.mutation_score
        print(f"Chromosome {self.thread_id} fitness: {self.fitness_score:.3f} (branch={self.branch_coverage:.2f}, line={self.line_coverage:.2f}, mutation={self.mutation_score:.2f})")

    def crossover(self, other):
        """
        Perform EvoSuite-style test suite crossover with another chromosome.
        Returns two offspring Chromosome instances.
        """
        # Read Java code
        def read_code(path):
            return read_java_file_as_string(path)

        java_code_1 = read_code(self.test_file_path)
        java_code_2 = read_code(other.test_file_path)

        # Extract full method bodies and names
        def extract_test_methods(code):
            method_pattern = re.compile(r'@Test\s+public\s+void\s+(\w+)\s*\(.*?\)\s*\{', re.MULTILINE)
            methods = []
            for match in method_pattern.finditer(code):
                method_name = match.group(1)
                start = match.start()
                open_braces = 0
                end = start
                for i in range(start, len(code)):
                    if code[i] == '{':
                        open_braces += 1
                    elif code[i] == '}':
                        open_braces -= 1
                        if open_braces == 0:
                            end = i + 1
                            break
                method_body = code[start:end]
                methods.append((method_name, method_body))
            return methods

        test_methods_1 = extract_test_methods(java_code_1)
        test_methods_2 = extract_test_methods(java_code_2)

        # Get method names for collision detection
        method_names_1 = {name for name, _ in test_methods_1}

        # Get crossover points
        α = random.random()
        split1 = int(α * len(test_methods_1))
        split2 = int(α * len(test_methods_2))

        # Apply name collision logic
        def resolve_conflicts(test_list, existing_names):
            renamed = []
            for name, body in test_list:
                original_name = name
                while name in existing_names:
                    name += "_co"
                if name != original_name:
                    body = re.sub(rf'\b{original_name}\b', name, body)
                existing_names.add(name)
                renamed.append(body)
            return renamed

        # Build test lists
        child1_tests = resolve_conflicts(test_methods_1[:split1], set()) + \
                       resolve_conflicts(test_methods_2[split2:], set(method_names_1))
        child2_tests = resolve_conflicts(test_methods_2[:split2], set()) + \
                       resolve_conflicts(test_methods_1[split1:], set(n for n, _ in test_methods_2))

        # Extract imports and fields
        def extract_imports(code):
            return re.findall(r'^import .*?;', code, re.MULTILINE)

        def extract_fields(code):
            return re.findall(r'^\s*(private|public|protected)?\s+\w+\s+\w+\s*;', code, re.MULTILINE)

        imports1 = extract_imports(java_code_1)
        imports2 = extract_imports(java_code_2)
        fields1 = extract_fields(java_code_1)
        fields2 = extract_fields(java_code_2)

        def build_offspring_code(imports1, imports2, fields1, fields2, methods, base_class_name):
            merged_imports = sorted(set(imports1 + imports2))
            merged_fields = sorted(set(fields1 + fields2))
            class_name = base_class_name

            code = "\n".join(merged_imports) + "\n\n"
            code += f"public class {class_name} {{\n\n"
            for field in merged_fields:
                code += f"    {field}\n"
            code += "\n"
            for method in methods:
                code += f"    {method}\n\n"
            code += "}"
            return code

        # Extract class name
        class_base = re.search(r'public class (\w+)', java_code_1)
        base_name = class_base.group(1) if class_base else "MergedTest"

        # Final offspring
        offspring_code_1 = build_offspring_code(imports1, imports2, fields1, fields2, child1_tests, base_name)
        offspring_code_2 = build_offspring_code(imports1, imports2, fields1, fields2, child2_tests, base_name)

        return offspring_code_1, offspring_code_2

    def mutate(self):
        """
        Randomly modify the unit test to increase diversity.
        Could add/remove assertions, tweak literals, etc.
        """
        raise NotImplementedError

    def __str__(self):
        return f"<Chromosome from {self.path}, Fitness={self.fitness_score}>"


    def __hash__(self):
        return hash(self.test_file_path)

    def __eq__(self, other):
        if not isinstance(other, Chromosome):
            return False
        return self.test_file_path == other.test_file_path