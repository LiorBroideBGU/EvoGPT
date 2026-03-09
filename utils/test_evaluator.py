import os
import re
import shutil
import subprocess
from pathlib import Path
from config.config_loader import get_config
from utils.JavaCodeCoverage.Jacoco import JavaCodeCoverage
from utils.MutationScoreGenerator.PITest import PITestRunner
from app.chromosome import extract_package_from_path


def _detect_package(java_file):
    """Read a Java file and extract its package declaration."""
    try:
        with open(java_file, 'r') as f:
            for line in f:
                m = re.match(r'^\s*package\s+([\w.]+)\s*;', line)
                if m:
                    return m.group(1)
    except Exception:
        pass
    return None


def _find_failing_tests(junit_output):
    """
    Parse JUnit console output to extract names of failing test methods.
    JUnit 4 reports failures like:
        1) testFoo(com.example.MyTest)
    """
    failing = set()
    for m in re.finditer(r'\d+\)\s+(\w+)\(', junit_output):
        failing.add(m.group(1))
    return failing


def _remove_test_methods(java_source_path, method_names):
    """
    Remove specific @Test methods from a Java test file by name.
    Uses brace-counting to handle nested blocks (try/catch etc.).
    Returns the number of methods removed.
    """
    with open(java_source_path, 'r') as f:
        lines = f.readlines()

    removed = 0
    for name in method_names:
        new_lines = []
        i = 0
        while i < len(lines):
            # Look for @Test annotation followed by a method with the target name
            if re.match(r'\s*@Test\b', lines[i]):
                # Scan ahead to find the method signature
                block_start = i
                j = i
                found = False
                while j < min(i + 5, len(lines)):
                    if re.search(r'public\s+void\s+' + re.escape(name) + r'\s*\(', lines[j]):
                        found = True
                        break
                    j += 1

                if found:
                    # Find the opening brace
                    while j < len(lines) and '{' not in lines[j]:
                        j += 1
                    # Count braces to find the end of the method
                    depth = 0
                    while j < len(lines):
                        for ch in lines[j]:
                            if ch == '{':
                                depth += 1
                            elif ch == '}':
                                depth -= 1
                        j += 1
                        if depth == 0:
                            break
                    removed += 1
                    i = j
                    continue

            new_lines.append(lines[i])
            i += 1

        lines = new_lines

    with open(java_source_path, 'w') as f:
        f.writelines(lines)

    return removed


class TestEvaluator:
    """
    Standalone evaluator: given a test file and a source file,
    measures branch coverage, line coverage, and mutation score
    using JaCoCo and PITest — only on passing tests.
    """

    def __init__(self, project_name, source_file_path, extra_classpath=None):
        self.project_name = project_name
        self.source_file_path = os.path.abspath(source_file_path)
        self.class_name = os.path.splitext(os.path.basename(source_file_path))[0]
        self.package_name = extract_package_from_path(source_file_path)
        self.fqn = f"{self.package_name}.{self.class_name}"

        self.lib_jars = os.path.abspath("lib/jars")
        self.extra_classpath = extra_classpath

    def evaluate(self, test_file_path, work_dir):
        """
        Evaluate a test file against the source.

        Returns:
            dict with keys: branch_coverage, line_coverage, mutation_score, test_strength
            Returns None on failure.
        """
        test_file_path = os.path.abspath(test_file_path)
        javafiles_dir = Path(work_dir) / "javafiles"
        classfiles_dir = Path(work_dir) / "classfiles"
        javafiles_dir.mkdir(parents=True, exist_ok=True)
        classfiles_dir.mkdir(parents=True, exist_ok=True)

        test_basename = os.path.basename(test_file_path)
        test_class_name_simple = os.path.splitext(test_basename)[0]

        test_pkg = _detect_package(test_file_path)

        # Only copy scaffolding if the test still references it
        with open(test_file_path, 'r') as f:
            test_content = f.read()
        needs_scaffolding = 'scaffolding' in test_content

        if test_pkg:
            pkg_subdir = os.path.join(javafiles_dir, test_pkg.replace('.', os.sep))
            os.makedirs(pkg_subdir, exist_ok=True)
            shutil.copy(self.source_file_path, os.path.join(pkg_subdir, f"{self.class_name}.java"))
            shutil.copy(test_file_path, os.path.join(pkg_subdir, test_basename))
            if needs_scaffolding:
                scaffolding = test_file_path.replace("_ESTest.java", "_ESTest_scaffolding.java")
                if os.path.exists(scaffolding):
                    shutil.copy(scaffolding, os.path.join(pkg_subdir, os.path.basename(scaffolding)))
            test_fqn = f"{test_pkg}.{test_class_name_simple}"
            source_dir_for_jacoco = Path(pkg_subdir)
            test_java_in_workdir = os.path.join(pkg_subdir, test_basename)
        else:
            shutil.copy(self.source_file_path, os.path.join(javafiles_dir, f"{self.class_name}.java"))
            shutil.copy(test_file_path, os.path.join(javafiles_dir, test_basename))
            if needs_scaffolding:
                scaffolding = test_file_path.replace("_ESTest.java", "_ESTest_scaffolding.java")
                if os.path.exists(scaffolding):
                    shutil.copy(scaffolding, os.path.join(javafiles_dir, os.path.basename(scaffolding)))
            test_fqn = test_class_name_simple
            source_dir_for_jacoco = Path(javafiles_dir)
            test_java_in_workdir = os.path.join(javafiles_dir, test_basename)

        cp = self._build_classpath()

        java_files = self._collect_java_files(javafiles_dir)

        if not self._compile(java_files, cp, classfiles_dir):
            return None

        # --- Pre-run: discover failing tests and strip them ---
        failing = self._discover_failing_tests(classfiles_dir, cp, test_fqn)
        if failing:
            print(f"[Evaluator] Removing {len(failing)} failing tests: {failing}")
            removed = _remove_test_methods(test_java_in_workdir, failing)
            print(f"[Evaluator] Removed {removed} test methods, recompiling...")

            shutil.rmtree(classfiles_dir)
            os.makedirs(classfiles_dir, exist_ok=True)
            java_files = self._collect_java_files(javafiles_dir)
            if not self._compile(java_files, cp, classfiles_dir):
                return None

        branch_cov, line_cov = self._run_jacoco(
            source_dir_for_jacoco, javafiles_dir, classfiles_dir, test_fqn, cp
        )
        mut_score, test_strength = self._run_pitest(
            classfiles_dir, javafiles_dir, test_fqn
        )

        return {
            "branch_coverage": branch_cov,
            "line_coverage": line_cov,
            "mutation_score": mut_score,
            "test_strength": test_strength,
        }

    def _collect_java_files(self, javafiles_dir):
        java_files = []
        for root, dirs, files in os.walk(javafiles_dir):
            for f in files:
                if f.endswith('.java'):
                    java_files.append(os.path.join(root, f))
        return java_files

    def _compile(self, java_files, cp, classfiles_dir):
        cfg = get_config()
        try:
            subprocess.run(
                [cfg.JAVAC_BIN, "-cp", cp, "-d", classfiles_dir] + java_files,
                check=True, capture_output=True, text=True
            )
            return True
        except subprocess.CalledProcessError as e:
            print(f"[Evaluator] Compilation failed: {e.stderr[:500]}")
            return False

    def _discover_failing_tests(self, classfiles_dir, cp, test_fqn):
        """Run tests once without JaCoCo to find which methods fail."""
        cfg = get_config()
        try:
            result = subprocess.run(
                [
                    cfg.JAVA_BIN,
                    "-cp", f"{classfiles_dir}{os.pathsep}{cp}",
                    "org.junit.runner.JUnitCore", test_fqn,
                ],
                capture_output=True, text=True, timeout=120,
            )
        except subprocess.TimeoutExpired:
            print("[Evaluator] Pre-run timed out")
            return set()

        if result.returncode == 0:
            return set()

        combined = result.stdout + result.stderr
        failing = _find_failing_tests(combined)
        return failing

    def _build_classpath(self):
        jars = [os.path.join(self.lib_jars, f)
                for f in os.listdir(self.lib_jars) if f.endswith('.jar')]
        if self.extra_classpath:
            if isinstance(self.extra_classpath, list):
                jars.extend(self.extra_classpath)
            else:
                jars.append(self.extra_classpath)
        return os.pathsep.join(jars)

    def _run_jacoco(self, source_dir: Path, javafiles_root: Path, classfiles_dir: Path, test_fqn: str, cp: str):
        """
        source_dir: directory containing the actual {ClassName}.java (for parsing)
        javafiles_root: root of the java source tree (for JaCoCo sourcefiles)
        """
        jacoco_agent = Path(self.lib_jars) / "jacocoagent.jar"
        jacoco_cli = Path(self.lib_jars) / "jacococli.jar"
        coverage_exec = classfiles_dir / "coverage.exec"
        coverage_xml = classfiles_dir / "coverage.xml"

        cfg = get_config()
        try:
            result = subprocess.run(
                [
                    cfg.JAVA_BIN,
                    f"-javaagent:{jacoco_agent}=destfile={coverage_exec}",
                    "-cp", f"{classfiles_dir}{os.pathsep}{cp}",
                    "org.junit.runner.JUnitCore", test_fqn,
                ],
                capture_output=True, text=True, timeout=120,
            )
            if result.returncode != 0:
                print(f"[Evaluator] Some tests failed during JaCoCo run (exit {result.returncode})")
        except subprocess.TimeoutExpired:
            print("[Evaluator] JaCoCo test run timed out")
            return 0.0, 0.0

        if not coverage_exec.exists():
            print("[Evaluator] No coverage.exec generated")
            return 0.0, 0.0

        try:
            subprocess.run(
                [
                    cfg.JAVA_BIN, "-jar", jacoco_cli,
                    "report", str(coverage_exec),
                    "--classfiles", str(classfiles_dir),
                    "--sourcefiles", str(javafiles_root),
                    "--xml", str(coverage_xml),
                ],
                check=True, capture_output=True, text=True,
            )
        except subprocess.CalledProcessError as e:
            print(f"[Evaluator] JaCoCo report generation failed: {e}")
            return 0.0, 0.0

        jcc = JavaCodeCoverage(source_dir, self.class_name, self.project_name)
        try:
            coverage_data, _ = jcc.parse_jacoco_xml(coverage_xml)
        except Exception as e:
            print(f"[Evaluator] JaCoCo XML parse failed: {e}")
            return 0.0, 0.0

        total_branch, total_line, count = 0.0, 0.0, 0
        for cls, methods in coverage_data.items():
            if "Test" in cls or "ESTest" in cls:
                continue
            for method, metrics in methods.items():
                total_branch += metrics.get("branch_coverage", 100)
                total_line += metrics.get("line_coverage", 0)
                count += 1

        if count == 0:
            return 0.0, 0.0
        return total_branch / count, total_line / count

    def _run_pitest(self, classfiles_dir, source_dir, test_fqn):
        report_dir = os.path.join(os.path.dirname(classfiles_dir), "pitest_report")
        runner = PITestRunner(
            project_name=self.project_name,
            class_name=self.fqn,
            classfiles_dir=classfiles_dir,
            source_dir=source_dir,
            report_dir=report_dir,
        )
        try:
            mutation_score, test_strength = runner.run(test_fqn)
        except Exception as e:
            print(f"[Evaluator] PITest failed: {e}")
            return 0.0, 0.0
        return mutation_score, test_strength
