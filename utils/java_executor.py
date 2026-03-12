import os
import subprocess
import javalang
from config.config_loader import get_config
from pathlib import Path


class JavaExecutor:
    def __init__(self, java_file_path: Path | str):
        self.java_file_path = Path(java_file_path)

        jars_dir = Path("lib", "jars")
        jar_files = [p.name for p in jars_dir.rglob("*.jar")]
        # Classpath as OS-specific path separator–joined string
        self.classpath = os.pathsep.join(str(jars_dir / p) for p in jar_files)

        with self.java_file_path.open("r", encoding="utf-8") as file:
            self.java_code = file.read()

        self.java_file_name = self.java_file_path.stem
        self.output_dir = self.java_file_path.parent.parent / "classfiles"
        self.output_dir.mkdir(parents=True, exist_ok=True)

    def check_java_code_syntax(self):
        """
        Checks if the java code is syntactically correct.
        :return:
        """
        try:
            javalang.parse.parse(self.java_code)
        except Exception as e:
            raise SyntaxError(f"Syntax error in java code: {e}")

    def compile_java(self):
        """
        Compiles the java file retrieved from the java file path, using the dependent .jar files as well
        :return: Boolean whether compilation was successful
        :return: Stack-trace from the compiler process.
        """
        cfg = get_config()
        try:
            result = subprocess.run(
                [
                    cfg.JAVAC_BIN,
                    "-cp", self.classpath,
                    str(self.java_file_path),
                    "-d", str(self.output_dir),

                ],
                capture_output=True,
                text=True
            )
            if result.returncode != 0:
                return False, result.stderr

            return True, 'Compilation successful'

        except Exception as e:
            return False, str(e)

    def get_fully_qualified_class_name(self) -> str:
        """
        Returns the fully qualified class name for the Java test class.
        If the source file declares a package, the package name is prefixed.
        Otherwise, the simple class name (file stem) is returned.
        """
        try:
            tree = javalang.parse.parse(self.java_code)
            package_name = getattr(getattr(tree, "package", None), "name", None)
            if package_name:
                return f"{package_name}.{self.java_file_name}"
        except Exception:
            # Fall back to simple class name if parsing fails for any reason
            pass

        return self.java_file_name

    def run_java(self):
        """
        Compiles and runs a Java class using JUnit.
        :return: (success, output) tuple
        """
        cfg = get_config()
        try:
            compilation_successful, error = self.compile_java()
            if not compilation_successful:
                return False, f"Compilation failed:\n{error}"

            fully_qualified_name = self.get_fully_qualified_class_name()

            # Run the compiled tests
            result = subprocess.run(
                [
                    cfg.JAVA_BIN,
                    "-cp", f"{str(self.output_dir)}{os.pathsep}{self.classpath}",
                    "org.junit.runner.JUnitCore",
                    fully_qualified_name
                ],
                capture_output=True,
                text=True
            )
            if result.returncode != 0:
                return False, result.stdout

            return True, result.stdout
        except Exception as e:
            return False, str(e)