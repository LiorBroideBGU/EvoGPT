import subprocess
import shutil
import os


class MutationAssertionGenerator:
    def __init__(self, classfiles_dir, java_file_path, output_dir):
        """
        Initializes the MutationAssertionGenerator.

        :param classfiles_dir: Path to the compiled Java classes.
        :param java_file_path: Path to the Java file being tested.
        :param output_dir: Directory where the modified test will be saved.
        :param evosuite_jar: Path to the EvoSuite JAR file.
        """
        self.evosuite_jar = os.path.abspath(r'C:\Users\liorb\PycharmProjects\EvoChat\sbst_agents\lib\evosuite-1.0.6.jar')
        self.classfiles_dir = os.path.abspath(classfiles_dir)
        self.output_dir = os.path.abspath(output_dir)

        # Java setup
        self.java_bin = r"C:\Program Files\Java\jdk1.8.0_202\bin\java.exe"
        self.tools_jar = r"C:\Program Files\Java\jdk1.8.0_202\lib\tools.jar"

        # Extract class name and package from Java file
        self.java_file_name = os.path.basename(java_file_path).replace(".java", "")
        with open(java_file_path, "r", encoding="utf-8") as file:
            java_code = file.read()
        self.package_name = self.extract_package_name(java_code)
        self.fully_qualified_name = f"{self.package_name}.{self.java_file_name}" if self.package_name else self.java_file_name

        # Set up classpath (compiled classes + lib JARs + tools.jar)
        self.lib_jars_path = os.path.abspath(os.path.join("lib", "jars"))
        jar_files = [os.path.join(self.lib_jars_path, f) for f in os.listdir(self.lib_jars_path) if f.endswith('.jar')]

        self.classpath = os.pathsep.join([self.classfiles_dir] + jar_files + [self.tools_jar])

        # Ensure output directory exists
        os.makedirs(self.output_dir, exist_ok=True)

    @staticmethod
    def extract_package_name(java_code):
        """Extracts package name from Java source code."""
        for line in java_code.splitlines():
            line = line.strip()
            if line.startswith("package "):
                return line.split(" ")[1].rstrip(";")
        return ""

    def run_mutation_assertions(self):
        """
        Runs EvoSuite mutation assertion generation on the provided test class.
        """
        command = [
            self.java_bin,
            f"-Dtools_jar_location={self.tools_jar}",
            "-cp", f"{self.evosuite_jar}{os.pathsep}{self.classpath}",
            "org.evosuite.EvoSuite",
            "-class", self.fully_qualified_name,  # Fully qualified class name
            "-projectCP", self.classfiles_dir,
            "-Dassertion_strategy=MUTATION"
        ]

        try:
            process = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
            print("EvoSuite Output:\n", process.stdout)
            print("EvoSuite Errors:\n", process.stderr)

            # Check if EvoSuite completed successfully
            if process.returncode == 0:
                print(f"Mutation assertions generated successfully for {self.fully_qualified_name}.")
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
        package_path = self.package_name.replace(".", "/") if self.package_name else ""
        generated_test_file = os.path.join(generated_test_dir, package_path, f"{self.java_file_name}_ESTest.java")

        if os.path.exists(generated_test_file):
            final_path = os.path.join(self.output_dir, os.path.basename(generated_test_file))
            shutil.move(generated_test_file, final_path)
            print(f"Modified test saved to: {final_path}")
        else:
            print("Error: Modified test file not found.")
