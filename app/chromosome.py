import os
from utils.JavaCodeCoverage.Jacoco import JavaCodeCoverage
from utils.MutationScoreGenerator.PITest import PITestRunner

class Chromosome:
    def __init__(self, path: str, thread_id: int = None):
        """
        Initialize a chromosome representing a unit test file.
        :param path: The folder where this unit test lives (e.g. .../JsonArray/3).
        """
        self.path = path
        self.java_file_name = path.split('\\')[-3]
        self.thread_id = thread_id
        self.test_file_path = self._locate_test_file()
        self.fitness_score = self.compute_fitness()  # To be computed
        self.coverage_metrics = None  # Optional: store detailed coverage breakdown
        self.mutation_score = None  # If using mutation testing later
        print(f"CHROMOSE GENERATION {thread_id}")


    def _locate_test_file(self):
        """
        Locate the main test file inside the directory.
        This assumes there's only one test file in the directory.
        """
        for file in os.listdir(f'{self.path}'):
            if file.endswith(".java") and "Test" in file:
                return os.path.join(self.path, file)
        raise FileNotFoundError(f"No Java test file found in {self.path}")

    def compute_fitness(self):
        """
        Compute fitness for this chromosome.
        Could be based on coverage, mutation score, assertions, etc.
        Needs access to JaCoCo/OpenClover report or metrics.
        """
        # Pseudocode:
        # Run coverage -> parse -> update self.fitness_score
        # Example: fitness = 0.7 * branch_coverage + 0.3 * line_coverage
        # You can also store detailed coverage info here
        jcc = JavaCodeCoverage(f"{self.path}", self.java_file_name, "gson", self.thread_id)
        mutation_scorer = PITestRunner(project_name="gson",
        class_name=f"com.google.gson.{self.java_file_name}",  # fully qualified class name
        classfiles_dir=f"{os.path.dirname(self.path)}\\classfiles",
        source_dir=self.path,
        report_dir=f"{os.path.dirname(self.path)}\\pitest_report")
        branch_coverage, line_coverage = jcc.get_average_coverage(thread_number=self.thread_id)
        mutation_score, test_strength = mutation_scorer.run(self.java_file_name + 'Test')
        fitness_score = 0.3 * branch_coverage + 0.2 * line_coverage + 0.4 * mutation_score + 0.1 * test_strength
        return fitness_score

    def crossover(self, other):
        """
        Combine with another chromosome (unit test) to produce offspring.
        Could swap assertions, test methods, etc.
        """
        raise NotImplementedError

    def mutate(self):
        """
        Randomly modify the unit test to increase diversity.
        Could add/remove assertions, tweak literals, etc.
        """
        raise NotImplementedError

    def __str__(self):
        return f"<Chromosome from {self.path}, Fitness={self.fitness_score}>"
