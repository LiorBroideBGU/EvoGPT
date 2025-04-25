import subprocess
import os
import xml.etree.ElementTree as ET
from config.config import JAVA_BIN, JAVAC_BIN

class JavaCodeCoverage:
    def __init__(self, java_files_dir, test_class, project_name, thread_id=None):
        """
        Initializes the JavaCodeCoverage class.

        :param java_files_dir: Directory containing both the test and source .java files.
        :param test_class: Name of the test class (e.g., JsonParserTest).
        :param project_name: Project name (e.g., gson).
        """
        self.java_files_dir = java_files_dir  # Path to your Java files (source + test)
        self.test_class = test_class  # Name of your test class (e.g., JsonParserTest)
        self.project_name = project_name  # Project name (e.g., gson)
        self.classpath = os.path.abspath(os.path.join("lib", "jars"))
        self.thread_id = thread_id
        # Collect all .jar files in the lib/jars directory to form the classpath
        jar_files = [f for f in os.listdir(self.classpath) if f.endswith('.jar')]
        self.classpath_combined = os.pathsep.join([os.path.join(self.classpath, jar) for jar in jar_files])

    def compile_java_files(self, output_dir):
        """
        Compiles the Java test and source files using javac.

        :param output_dir: Directory where the compiled .class files will be saved.
        :return: bool indicating success or failure of the compilation process.
        """
        try:
            subprocess.run(
                [
                    JAVAC_BIN,
                    "-cp", self.classpath_combined,  # Include all jars in the classpath
                    "-d", output_dir,  # Output directory for .class files
                    os.path.join(self.java_files_dir, f"{self.test_class}Test.java"),  # Test class
                    os.path.join(self.java_files_dir, f"{self.test_class}.java")
                    # Source class
                ],
                check=True,
                capture_output=True,
                text=True
            )
            print(f"Compilation successful: {output_dir}")
            return True
        except subprocess.CalledProcessError as e:
            print(f"Compilation failed: {e.stderr}")
            return False

    def run_tests_with_coverage(self, output_dir):
        """
        Runs the compiled tests with JaCoCo agent to collect coverage data.

        :param output_dir: Directory containing the compiled .class files.
        :return: bool indicating if the tests ran successfully and generated coverage data.
        """
        jacoco_agent = os.path.join(self.classpath, 'jacocoagent.jar')
        coverage_file = os.path.join(output_dir, "coverage.exec")

        try:
            result = subprocess.run(
                [
                    JAVA_BIN,
                    "-javaagent:" + jacoco_agent + f"=destfile={coverage_file}",  # JaCoCo agent argument
                    "-cp", f"{output_dir}{os.pathsep}{self.classpath_combined}",
                    "org.junit.runner.JUnitCore",  # Run the JUnit tests
                    self.test_class + 'Test'  # The test class name
                ],
                check=True,
                capture_output=True,
                text=True
            )
            print(f"Tests ran successfully. Coverage data saved in {coverage_file}.")
            return True
        except subprocess.CalledProcessError as e:

            print(f"Test run failed: {e.stderr}")
            return False

    def convert_exec_to_xml(self,output_dir):
        """
        Converts the JaCoCo .exec coverage file into an XML format using JaCoCo's CLI.

        :param output_dir: Directory to save the .exec coverage data.
        """
        try:
            subprocess.run(
                [
                    "java", "-jar", os.path.abspath(os.path.join('lib', 'jars', 'jacococli.jar')),
                    "report", os.path.join(output_dir, "coverage.exec"),  # .exec file to report on
                    "--classfiles",
                    os.path.join("results", "unit_tests", self.project_name, self.test_class, str(self.thread_id), "classfiles") if self.thread_id else os.path.join(os.path.dirname(self.java_files_dir), "classfiles"),
                    # Path to class files (compiled files)
                    "--sourcefiles",
                    os.path.join("results", "unit_tests", self.project_name, self.test_class, str(self.thread_id), "javafiles") if self.thread_id else self.java_files_dir,
                    # Path to the source files
                    "--xml", os.path.join(output_dir, "coverage.xml")  # Output in XML format
                ],
                check=True
            )
            print(f"Conversion to XML successful: {os.path.join(output_dir, "coverage.xml")}")
        except subprocess.CalledProcessError as e:
            print(f"Error converting .exec to .xml: {str(e)}")

    def generate_coverage_report(self):
        """
        Generates the coverage report by compiling, running tests with JaCoCo,
        and converting the .exec file to an XML report.
        """
        if self.thread_id:
            build_dir = os.path.join("results", "unit_tests", self.project_name, self.test_class, str(self.thread_id), "classfiles")
        else:
            parent_path = os.path.dirname(self.java_files_dir)
            build_dir  = os.path.join(parent_path, "classfiles")
        os.makedirs(build_dir, exist_ok=True)

        # Step 1: Compile the Java files
        if not self.compile_java_files(build_dir):
            return False

        # Step 2: Run the tests with JaCoCo agent to collect coverage data
        if not self.run_tests_with_coverage(build_dir):
            return False

        # Step 3: Convert the .exec file to XML format

        self.convert_exec_to_xml(build_dir)

        return True

    # JVM descriptor mapping
    JVM_TYPE_MAP = {
        'I': 'int', 'Z': 'boolean', 'D': 'double', 'F': 'float',
        'J': 'long', 'B': 'byte', 'C': 'char', 'S': 'short', 'V': 'void'
    }

    def parse_jvm_descriptor(self,desc: str):
        # JVM descriptor mapping
        JVM_TYPE_MAP = {
            'I': 'int', 'Z': 'boolean', 'D': 'double', 'F': 'float',
            'J': 'long', 'B': 'byte', 'C': 'char', 'S': 'short', 'V': 'void'
        }
        params = []
        i = 1  # skip '('
        while desc[i] != ')':
            array_dim = 0
            while desc[i] == '[':
                array_dim += 1
                i += 1

            if desc[i] == 'L':
                semicolon_index = desc.index(';', i)
                typename = desc[i + 1:semicolon_index].split('/')[-1]
                i = semicolon_index + 1
            else:
                typename = JVM_TYPE_MAP.get(desc[i], desc[i])
                i += 1

            typename += '[]' * array_dim
            params.append(typename)

        return params  # return_type is ignored

    def parse_jacoco_xml(self, xml_file):
        java_source_code = open(os.path.join(self.java_files_dir, f"{self.test_class}.java")).read()
        tree = ET.parse(xml_file)
        root = tree.getroot()

        coverage_results = {}
        missed_branches = {}

        for package in root.findall("package"):
            for class_element in package.findall("class"):
                class_name = class_element.get("name")
                if "Test" in class_name:
                    continue

                coverage_results[class_name] = {}

                for method in class_element.findall("method"):
                    method_name = method.get("name")
                    desc = method.get("desc", "")
                    param_types = self.parse_jvm_descriptor(desc)
                    full_signature = f"{method_name}({', '.join(param_types)})"

                    line_number = int(method.get("line", "-1"))

                    branch_counter = method.find("counter[@type='BRANCH']")
                    line_counter = method.find("counter[@type='LINE']")
                    if line_counter is not None:
                        lines_missed = int(line_counter.get("missed", "0"))
                        lines_covered = int(line_counter.get("covered", "0"))
                        total_lines = lines_missed + lines_covered
                        line_coverage = (lines_covered / total_lines) * 100 if total_lines > 0 else 0
                    else:
                        line_coverage = 0
                    if branch_counter is not None:
                        branches_missed = int(branch_counter.get("missed", "0"))
                        branches_covered = int(branch_counter.get("covered", "0"))
                        total_branches = branches_missed + branches_covered
                        branch_coverage = (branches_covered / total_branches) * 100 if total_branches > 0 else 100
                    else:
                        branch_coverage = 100 if line_coverage > 0 else 0

                    coverage_results[class_name][full_signature] = {
                        "branch_coverage": branch_coverage,
                        "line_coverage": line_coverage,
                    }

            for source_file in package.findall("sourcefile"):
                for line in source_file.findall("line"):
                    line_number = int(line.get("nr"))
                    missed_branches_count = int(line.get("mb", "0"))
                    if missed_branches_count > 0:
                        missed_branches[line_number] = missed_branches_count

        java_lines = java_source_code.split("\n")
        missed_branch_lines = {
            line_number: java_lines[line_number - 1].strip()
            for line_number in missed_branches if line_number <= len(java_lines)
        }

        return coverage_results, missed_branch_lines

    def get_average_coverage(self, thread_number=None):
        """
        Returns a tuple of (average_branch_coverage, average_line_coverage) across all non-test methods.
        Methods without any branches count as 100% branch coverage.
        """
        # Step 1: Generate coverage report
        success = self.generate_coverage_report()
        if not success:
            raise RuntimeError("Coverage report generation failed.")

        # Step 2: Determine XML path

        xml_path = os.path.join(
            "results", "unit_tests", self.project_name, self.test_class,
            str(thread_number),"classfiles", "coverage.xml"
        ) if thread_number else os.path.join(os.path.dirname(self.java_files_dir),"classfiles", "coverage.xml")
        if not os.path.exists(xml_path):
            raise FileNotFoundError(f"JaCoCo XML not found at: {xml_path}")

        # Step 3: Parse coverage data
        coverage_data, _ = self.parse_jacoco_xml(xml_path)

        total_branch = 0
        total_line = 0
        method_count = 0

        for class_name, methods in coverage_data.items():
            if "Test" in class_name:
                continue
            for method_name, metrics in methods.items():
                if "test" in method_name.lower():
                    continue

                # Count methods and accumulate
                branch_coverage = metrics.get("branch_coverage", 100)
                line_coverage = metrics.get("line_coverage", 0)

                total_branch += branch_coverage
                total_line += line_coverage
                method_count += 1

        if method_count == 0:
            return 0.0, 0.0  # or raise exception?

        avg_branch = total_branch / method_count
        avg_line = total_line / method_count

        return avg_branch, avg_line