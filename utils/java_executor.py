import os
import subprocess
import javalang
from config.config import JAVA_BIN, JAVAC_BIN

class JavaExecutor:
    def __init__(self, java_file_path):
        self.java_file_path = java_file_path
        self.javac_bin = JAVAC_BIN
        self.java_bin = JAVA_BIN

        self.classpath = os.path.abspath(os.path.join("lib", "jars"))
        jar_files = [f for f in os.listdir(self.classpath) if f.endswith('.jar')]

        # Construct the classpath by joining all JAR files
        self.classpath = os.pathsep.join([os.path.join(self.classpath, jar) for jar in jar_files])
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
            source_code_name = self.java_file_name.replace("Test", "")
            build_path = os.path.join(os.path.dirname(self.java_file_path))
            build_path = os.path.dirname(build_path)
            output_dir = f"{build_path}\\classfiles"
            os.makedirs(output_dir, exist_ok=True)
            result = subprocess.run(
                [
                    self.javac_bin,  # Explicitly use Java 8's `javac`
                    "-source", "1.8",
                    "-target", "1.8",
                    "-cp", self.classpath,
                    "-d", output_dir,
                    self.java_file_path
                ],
                capture_output=True,
                text=True
            )

            if result.returncode != 0:
                print("Compilation Error:", result.stderr)
                return False, result.stderr
            print("Java Compilation Successful")
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

            # Compile the Java file
            source_code_name = self.java_file_name.replace("Test", "")
            build_path = os.path.join(os.path.dirname(self.java_file_path))
            build_path = os.path.dirname(build_path)
            output_dir = f"{build_path}\\classfiles"
            os.makedirs(output_dir, exist_ok=True)
            compile_result = subprocess.run(
                [
                    self.javac_bin,  # Explicitly use Java 8's `javac`
                    "-source", "1.8",
                    "-target", "1.8",
                    "-cp", self.classpath,
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
                    self.java_bin,
                    "-cp", f"{output_dir}{os.pathsep}{self.classpath}",
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