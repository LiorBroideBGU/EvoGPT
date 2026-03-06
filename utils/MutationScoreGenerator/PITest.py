import os
import subprocess
from pathlib import Path
from config.config_loader import get_config
import re
import logging

class PITestRunner:
    def __init__(self, project_name: str, class_name: str, classfiles_dir: Path, source_dir: Path, report_dir: Path = Path("pitest_report")):
        self.logger = logging.getLogger(__name__)
        self.project_name = project_name
        self.class_name = class_name  # e.g., com.google.gson.JsonArray
        self.classfiles_dir = classfiles_dir.resolve()
        self.source_dir = source_dir.resolve()
        self.report_dir = report_dir.resolve()

        # Points to pitest-command-line.jar (not included in lib/jars)
        self.pitest_cli_jar = Path("utils/MutationScoreGenerator/jars/pitest-command-line-1.19.0.jar").resolve()
        
        # Check if PITest jar exists
        if not self.pitest_cli_jar.exists():
            self.logger.warning(f"PITest jar not found at {self.pitest_cli_jar}")
            self.logger.warning("Mutation testing will be skipped. Download from: https://github.com/hcoles/pitest/releases")
            self.pitest_available = False
        else:
            self.pitest_available = True

        self.jars_dir = Path("lib", "jars").resolve()
        self.classpath = self._build_classpath()

    def _build_classpath(self):
        all_jars = [str(self.jars_dir / f) for f in os.listdir(self.jars_dir) if f.endswith(".jar")]
        
        # Add PITest jars
        pitest_jars_dir = Path(self.pitest_cli_jar).parent
        if pitest_jars_dir.exists():
            pitest_jars = [str(pitest_jars_dir / f) for f in os.listdir(pitest_jars_dir) if f.endswith(".jar")]
            all_jars.extend(pitest_jars)
        
        return os.pathsep.join(all_jars)

    def run(self, test_class):
        # Skip if PITest jar is not available
        if not self.pitest_available:
            return 0.0, 0.0

        cfg = get_config()
        try:
            cmd = [
                cfg.JAVA_BIN,
                "-cp", self.classpath,
                "org.pitest.mutationtest.commandline.MutationCoverageReport",
                "--targetClasses", self.class_name,
                "--targetTests", test_class,
                "--classPath", self.classfiles_dir,
                "--sourceDirs", self.source_dir,
                "--reportDir", self.report_dir,
                "--threads", "2",  # Use 2 threads for faster execution
                "--timeoutFactor", "1.25",  # Faster timeout (default is 1.25, we make it explicit)
                "--timeoutConst", "3000",  # Max 3s per test
                "--mutators", "DEFAULTS"  # Use standard mutators only
                # Note: Keeping default output format to preserve console output for metric extraction
            ]

            result = subprocess.run(cmd, capture_output=True, text=True, timeout=60)
            
            # Debug output
            if result.returncode != 0:
                print(f"[PITest] Command failed with return code {result.returncode}")
                print(f"[PITest] STDERR: {result.stderr[:500]}")
                
            return self.extract_pitest_metrics(result.stdout)
        except subprocess.TimeoutExpired:
            print(f"[PITest] Timeout after 120 seconds")
            return 0.0, 0.0
        except Exception as e:
            print(f"[PITest] Error running mutation testing: {e}")
            import traceback
            traceback.print_exc()
            return 0.0, 0.0


    def extract_pitest_metrics(self,output_text):
        """
        Extracts mutation killed ratio and test strength percentage from PIT STDOUT.

        :param output_text: Full stdout text from PIT.
        :return: (mutation_killed_ratio: float, test_strength_percentage: float)
        """
        killed_ratio_match = re.search(r">> Generated \d+ mutations Killed \d+ \((\d+)%\)", output_text)
        test_strength_match = re.search(r">> Mutations with no coverage \d+\. Test strength (\d+)%", output_text)

        # Return 0.0 instead of None if regex doesn't match
        mutation_killed_ratio = float(killed_ratio_match.group(1)) if killed_ratio_match else 0.0
        test_strength_percentage = float(test_strength_match.group(1)) if test_strength_match else 0.0

        return mutation_killed_ratio, test_strength_percentage