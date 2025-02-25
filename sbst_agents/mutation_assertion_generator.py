import subprocess
import shutil
import os


class MutationAssertionGenerator:
    def __init__(self, java_file_path, output_dir):
        """
        Initializes the EvoSuiteMutationRunner.

        :param evosuite_jar: Path to the evosuite jar file.
        :param classpath: Path to the compiled Java classes.
        :param test_class: Fully qualified name of the test class.
        :param output_dir: Directory where the modified test will be saved.
        """
        self.evosuite_jar = os.path.join("sbst_agents", "lib", "evosuite-1.0.6.jar")
        self.classpath = os.path.abspath(os.path.join("lib", "jars"))
        jar_files = [f for f in os.listdir(self.classpath) if f.endswith('.jar')]
        self.java_file_name = os.path.basename(java_file_path).replace(".java", "")
        # Construct the classpath by joining all JAR files
        self.classpath = os.pathsep.join([os.path.join(self.classpath, jar) for jar in jar_files])
        self.output_dir = output_dir

        # Ensure output directory exists
        os.makedirs(self.output_dir, exist_ok=True)

    def run_mutation_assertions(self):
        """
        Runs EvoSuite mutation assertion generation on the provided test class.
        """
        command = [
            "java", "-jar", self.evosuite_jar,
            "-class", self.java_file_name,
            "-projectCP", self.classpath,
            "-Dassertion_strategy=MUTATION"
        ]

        try:
            process = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
            print("EvoSuite Output:\n", process.stdout)
            print("EvoSuite Errors:\n", process.stderr)

            # Check if EvoSuite completed successfully
            if process.returncode == 0:
                print(f"Mutation assertions generated successfully for {self.java_file_name}.")
                self.save_modified_test()
            else:
                print(f"Error during EvoSuite execution: {process.stderr}")

        except Exception as e:
            print(f"Error running EvoSuite: {str(e)}")

    def save_modified_test(self):
        """
        Moves the modified test file to the specified output directory.
        """
        generated_test_dir = os.path.join(os.getcwd(), "evosuite-tests")
        package_path = self.java_file_name.replace(".", "/")
        generated_test_file = os.path.join(generated_test_dir, package_path + "_ESTest.java")

        if os.path.exists(generated_test_file):
            final_path = os.path.join(self.output_dir, os.path.basename(generated_test_file))
            shutil.move(generated_test_file, final_path)
            print(f"Modified test saved to: {final_path}")
        else:
            print("Error: Modified test file not found.")

