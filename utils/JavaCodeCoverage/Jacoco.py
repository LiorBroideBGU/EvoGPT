import subprocess
import os
import xml.etree.ElementTree as ET


class JavaCodeCoverage:
    def __init__(self, java_files_dir, test_class, project_name):
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
                    "javac",
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
            subprocess.run(
                [
                    "java",
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
                    os.path.join("results", "unit_tests", self.project_name, self.test_class, "classfiles"),
                    # Path to class files (compiled files)
                    "--sourcefiles",
                    os.path.join("results", "unit_tests", self.project_name, self.test_class, "javafiles"),
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
        build_dir = os.path.join("results", "unit_tests", self.project_name, self.test_class, "classfiles")
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


    def parse_coverage(self, xml_file):
        tree = ET.parse(xml_file)
        root = tree.getroot()

        # Initialize a dictionary to hold the coverage data
        coverage_data = {}

        # Iterate over each class in the XML
        for class_elem in root.findall(".//class"):
            class_name = class_elem.get("name")

            # Iterate over methods in the class
            for method_elem in class_elem.findall("method"):
                method_name = method_elem.get("name")
                method_line = method_elem.get("line")

                # Get the coverage counters for the method
                line_coverage = {}
                branch_coverage = {}
                missed_branches = []

                # For each counter type in the method
                for counter_elem in method_elem.findall("counter"):
                    counter_type = counter_elem.get("type")
                    missed = int(counter_elem.get("missed"))
                    covered = int(counter_elem.get("covered"))

                    # Collect line and branch coverage data
                    if counter_type == "LINE":
                        line_coverage = {"missed": missed, "covered": covered}
                    elif counter_type == "BRANCH":
                        branch_coverage = {"missed": missed, "covered": covered}
                        # Find missed branches and store them
                        if missed > 0:
                            missed_branches.append(method_line)

                # Store the coverage data for the method
                coverage_data[f"{class_name}.{method_name}"] = {
                    "line_coverage": line_coverage,
                    "branch_coverage": branch_coverage,
                    "missed_branches": missed_branches
                }

        return coverage_data
