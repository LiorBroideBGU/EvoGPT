import os
import subprocess
import javalang

class JavaExecutor:
    def __init__(self, java_file_path):
        self.java_file_path = java_file_path

        self.classpath = os.path.abspath(os.path.join("..", "lib", "jars"))
        with open(self.java_file_path, "r", encoding="utf-8") as file:
            self.java_code = file.read()
        self.java_file_name = os.path.basename(java_file_path).replace(".java", "")

    def check_java_code_syntax(self):
        """
        Checks if the java code is syntactically correct.
        :param java_code:
        :return:
        """
        try:
            tree = javalang.parse.parse(self.java_code)
        except Exception as e:
            raise SyntaxError(f"Syntax error in java code: {e}")

    def compile_java(self):
        """
        Compiles the java file retrieved from the java file path, using the dependent .jar files as well
        :return: Boolean whether compilation was successful
        :return: Stack-trace from the compiler process.
        """
        try:
            jar_files = [f for f in os.listdir(self.classpath) if f.endswith('.jar')]

            # Construct the classpath by joining all JAR files
            self.classpath = os.pathsep.join([os.path.join(self.classpath, jar) for jar in jar_files])
            result = subprocess.run(
                ["javac", "-cp", self.classpath, self.java_file_path],
                capture_output=True,
                text=True
            )

            if result.returncode != 0:
                return False, result.stderr
            return True, 'Compilation successful'

        except Exception as e:
            return False, str(e)

    def run_java(self):
        """
        Compiles and runs a Java class using JUnit.
        :param java_file: Path to the Java file to compile and run.
        :param classpath: Path to the directory containing JAR dependencies.
        :return: (success, output) tuple
        """
        try:
            # Ensure the classpath includes all JAR files
            jar_files = [os.path.join(self.classpath, f) for f in os.listdir(self.classpath) if f.endswith('.jar')]
            classpath_combined = os.pathsep.join(jar_files)

            # Compile the Java file
            output_dir = f"build\\{self.java_file_name}"
            os.makedirs(output_dir, exist_ok=True)
            compile_result = subprocess.run(
                [
                    "javac",
                    "-cp", classpath_combined,
                    "-d", output_dir,
                    self.java_file_path
                ],
                capture_output=True,
                text=True
            )

            if compile_result.returncode != 0:
                return False, f"Compilation Error:\n{compile_result.stderr}"


            # Run the compiled tests
            result = subprocess.run(
                [
                    "java",
                    "-cp", f"{output_dir}{os.pathsep}{classpath_combined}",
                    "org.junit.runner.JUnitCore",
                    self.java_file_name
                ],
                capture_output=True,
                text=True
            )

            if result.returncode != 0:
                print(f"Runtime Error:\n{result.stdout}")
                return False, result.stdout

            return True, result.stdout
        except Exception as e:
            return False, str(e)