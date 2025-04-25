import os
import subprocess
from config.config import JAVA_BIN, JAVAC_BIN
import re
class PITestRunner:
    def __init__(self, project_name, class_name, classfiles_dir, source_dir, report_dir="pitest_report"):
        self.project_name = project_name
        self.class_name = class_name  # e.g., com.google.gson.JsonArray
        self.classfiles_dir = os.path.abspath(classfiles_dir)
        self.source_dir = os.path.abspath(source_dir)
        self.report_dir = os.path.abspath(report_dir)

        # Points to pitest-command-line.jar (not included in lib/jars)
        self.pitest_cli_jar = os.path.abspath("utils/MutationScoreGenerator/jars/pitest-command-line-1.19.0.jar")

        self.jars_dir = os.path.abspath("lib/jars")
        self.classpath = self._build_classpath()

    def _build_classpath(self):
        all_jars = [os.path.join(self.jars_dir, f) for f in os.listdir(self.jars_dir) if f.endswith(".jar")]
        all_jars.append(self.pitest_cli_jar)  # Add command-line CLI JAR
        return os.pathsep.join(all_jars)

    def run(self, test_class):
        try:
            cmd = [
                JAVA_BIN,
                "-cp", self.classpath,
                "org.pitest.mutationtest.commandline.MutationCoverageReport",
                "--targetClasses", self.class_name,
                "--targetTests", test_class,
                "--classPath", self.classfiles_dir,
                "--sourceDirs", self.source_dir,
                "--reportDir", self.report_dir
            ]

            print("[PITest] Running command:\n", " ".join(cmd))
            result = subprocess.run(cmd, capture_output=True, text=True)
            return self.extract_pitest_metrics(result.stdout)
        except Exception as e:
            print(f"[PITest] Error running mutation testing: {e}")
            return False


    def extract_pitest_metrics(self,output_text):
        """
        Extracts mutation killed ratio and test strength percentage from PIT STDOUT.

        :param output_text: Full stdout text from PIT.
        :return: (mutation_killed_ratio: float, test_strength_percentage: float)
        """
        killed_ratio_match = re.search(r">> Generated \d+ mutations Killed \d+ \((\d+)%\)", output_text)
        test_strength_match = re.search(r">> Mutations with no coverage \d+\. Test strength (\d+)%", output_text)

        mutation_killed_ratio = int(killed_ratio_match.group(1)) if killed_ratio_match else None
        test_strength_percentage = int(test_strength_match.group(1)) if test_strength_match else None

        return mutation_killed_ratio, test_strength_percentage