import os
import subprocess
class JavaExecutor:
    def __init__(self, java_file_path, classpath):
        self.java_file_path = java_file_path
        self.classpath = classpath
        with open(self.java_file_path, "r", encoding="utf-8") as file:
            self.java_code = file.read()
        self.java_file_name = os.path.basename(java_file_path).replace(".java", "")

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

