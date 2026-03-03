import os
import subprocess
import shutil
import urllib.request
from config.config import JAVA_BIN, JAVAC_BIN

EVOSUITE_VERSION = "1.2.0"
EVOSUITE_JAR_URL = f"https://github.com/EvoSuite/evosuite/releases/download/v{EVOSUITE_VERSION}/evosuite-{EVOSUITE_VERSION}.jar"
EVOSUITE_RUNTIME_JAR_URL = f"https://github.com/EvoSuite/evosuite/releases/download/v{EVOSUITE_VERSION}/evosuite-standalone-runtime-{EVOSUITE_VERSION}.jar"

JARS_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "jars")


def download_evosuite_jars(force=False):
    os.makedirs(JARS_DIR, exist_ok=True)
    jar_path = os.path.join(JARS_DIR, f"evosuite-{EVOSUITE_VERSION}.jar")
    runtime_path = os.path.join(JARS_DIR, f"evosuite-standalone-runtime-{EVOSUITE_VERSION}.jar")

    for url, dest in [(EVOSUITE_JAR_URL, jar_path), (EVOSUITE_RUNTIME_JAR_URL, runtime_path)]:
        if os.path.exists(dest) and not force:
            print(f"[EvoSuite] Already exists: {os.path.basename(dest)}")
            continue
        print(f"[EvoSuite] Downloading {os.path.basename(dest)}...")
        urllib.request.urlretrieve(url, dest)
        print(f"[EvoSuite] Saved to {dest}")

    return jar_path, runtime_path


class EvoSuiteRunner:
    def __init__(self, project_name, class_path_source, output_base_dir="comparison_results"):
        self.project_name = project_name
        self.class_path_source = os.path.abspath(class_path_source)

        filename = os.path.basename(class_path_source)
        self.class_name = os.path.splitext(filename)[0]
        self.fqn = self._extract_fqn(class_path_source)

        self.evosuite_jar = os.path.join(JARS_DIR, f"evosuite-{EVOSUITE_VERSION}.jar")
        self.runtime_jar = os.path.join(JARS_DIR, f"evosuite-standalone-runtime-{EVOSUITE_VERSION}.jar")
        self.output_base_dir = output_base_dir

        self.lib_jars_dir = os.path.abspath("lib/jars")

        if not os.path.exists(self.evosuite_jar):
            raise FileNotFoundError(
                f"EvoSuite JAR not found at {self.evosuite_jar}. "
                "Run with --download-evosuite first."
            )

    def _extract_fqn(self, java_file_path):
        normalized = java_file_path.replace('\\', '/')
        if '/src/main/java/' in normalized:
            rel = normalized.split('/src/main/java/')[1]
        elif '/src/' in normalized:
            rel = normalized.split('/src/')[1]
        else:
            parts = normalized.split('/')
            rel = '/'.join(parts[-4:])

        rel = rel.replace('.java', '').replace('/', '.')
        return rel

    def _find_source_root(self):
        """Find the project source root (e.g. .../src/main/java/)."""
        normalized = self.class_path_source.replace('\\', '/')
        if '/src/main/java/' in normalized:
            return normalized.split('/src/main/java/')[0] + '/src/main/java/'
        if '/src/' in normalized:
            return normalized.split('/src/')[0] + '/src/'
        return os.path.dirname(self.class_path_source)

    def _compile_source_for_evosuite(self, working_dir):
        """
        Compile ALL project source files with Java 11 to produce classfiles
        compatible with EvoSuite's ASM version. This avoids needing the
        pre-built JAR (which may be compiled with a newer Java) on EvoSuite's
        classpath.
        """
        classfiles_dir = os.path.join(working_dir, "compiled_source")
        os.makedirs(classfiles_dir, exist_ok=True)

        dep_jars = [os.path.join(self.lib_jars_dir, f)
                    for f in os.listdir(self.lib_jars_dir) if f.endswith('.jar')]

        src_root = self._find_source_root()
        java_files = []
        for root, dirs, files in os.walk(src_root):
            for f in files:
                if f.endswith('.java'):
                    java_files.append(os.path.join(root, f))

        if not java_files:
            print(f"[EvoSuite] No .java files found under {src_root}")
            return None

        try:
            subprocess.run(
                [JAVAC_BIN, "-source", "11", "-target", "11",
                 "-cp", os.pathsep.join(dep_jars),
                 "-d", classfiles_dir] + java_files,
                check=True, capture_output=True, text=True,
            )
        except subprocess.CalledProcessError as e:
            print(f"[EvoSuite] Source compilation failed: {e.stderr[:500]}")
            return None

        return classfiles_dir

    def generate_tests(self, budget, budget_type="time", working_dir=None):
        """
        Run EvoSuite to generate tests.

        Args:
            budget: Budget value (seconds for time, count for generations)
            budget_type: 'time' or 'generations'
            working_dir: Directory where EvoSuite will run and output tests

        Returns:
            Path to the generated test file, or None on failure
        """
        if working_dir is None:
            working_dir = os.path.join(
                self.output_base_dir, "evosuite_runs",
                self.project_name, self.class_name,
                f"{budget_type}_{budget}"
            )
        os.makedirs(working_dir, exist_ok=True)

        compiled_dir = self._compile_source_for_evosuite(working_dir)
        if compiled_dir is None:
            print("[EvoSuite] Cannot proceed without compiled source.")
            return None

        project_cp = os.path.abspath(compiled_dir)
        test_dir = os.path.abspath(os.path.join(working_dir, 'evosuite-tests'))

        cmd = [
            JAVA_BIN, "-jar", os.path.abspath(self.evosuite_jar),
            "-class", self.fqn,
            "-projectCP", project_cp,
            f"-Dtest_dir={test_dir}",
        ]

        if budget_type == "generations":
            cmd.extend([
                "-Dstopping_condition=MAXGENERATIONS",
                f"-Dsearch_budget={budget}",
            ])
        else:
            cmd.extend([
                "-Dstopping_condition=MAXTIME",
                f"-Dsearch_budget={budget}",
            ])

        cmd.extend([
            "-Dminimize=true",
            "-Dassertion_strategy=ALL",
        ])

        print(f"[EvoSuite] Running: {self.fqn} budget={budget} ({budget_type})")
        try:
            result = subprocess.run(
                cmd, capture_output=True, text=True,
                timeout=max(budget * 3, 120),
                cwd=working_dir
            )
            if result.returncode != 0:
                print(f"[EvoSuite] STDERR: {result.stderr[:1000]}")
        except subprocess.TimeoutExpired:
            print(f"[EvoSuite] Timeout after {max(budget * 3, 120)}s")
            return None
        except Exception as e:
            print(f"[EvoSuite] Error: {e}")
            return None

        test_dir = os.path.join(working_dir, "evosuite-tests")
        test_file = self._find_test_file(test_dir)
        if test_file is None:
            print(f"[EvoSuite] No test file generated in {test_dir}")
            return None

        print(f"[EvoSuite] Test generated: {test_file}")
        self._sanitize_test_for_jacoco(test_file)
        return test_file

    def _sanitize_test_for_jacoco(self, test_file):
        """
        Transform an EvoSuite test into a fully standalone JUnit 4 test
        with zero EvoSuite runtime dependencies, so JaCoCo and PITest work.
        """
        import re
        with open(test_file, 'r') as f:
            lines = f.readlines()

        content = ''.join(lines)

        # --- Pass 1: remove entire test methods that catch NoClassDefFoundError ---
        # These are artifacts of EvoSuite's separateClassLoader and will
        # always fail outside the EvoRunner.
        removed_methods = 0
        while True:
            m = re.search(r'(\s*)@Test\b[^\n]*\n\s*public\s+void\s+\w+\s*\(\)\s*(?:throws\s+\w+\s*)?\{', content)
            if not m:
                break
            # Find the method body end using character-level brace counting
            brace_start = content.index('{', m.start())
            depth = 0
            end = brace_start
            for idx in range(brace_start, len(content)):
                if content[idx] == '{':
                    depth += 1
                elif content[idx] == '}':
                    depth -= 1
                    if depth == 0:
                        end = idx + 1
                        break
            method_body = content[m.start():end]
            if 'NoClassDefFoundError' in method_body:
                content = content[:m.start()] + content[end:]
                removed_methods += 1
            else:
                # Not a NoClassDefFoundError method — skip past it so we don't
                # re-match the same @Test. Replace @Test temporarily with a marker.
                content = content[:m.start()] + content[m.start():end].replace('@Test', '@_KEEP_', 1) + content[end:]

        content = content.replace('@_KEEP_', '@Test')

        if removed_methods:
            print(f"[EvoSuite] Removed {removed_methods} NoClassDefFoundError test methods")

        # --- Pass 1b: remove test methods using EvoSuite's shaded Mockito ---
        removed_mock = 0
        content = content.replace('@_KEEP_', '@Test')
        while True:
            m = re.search(r'(\s*)@Test\b[^\n]*\n\s*public\s+void\s+\w+\s*\(\)\s*(?:throws\s+\w+\s*)?\{', content)
            if not m:
                break
            brace_start = content.index('{', m.start())
            depth = 0
            end = brace_start
            for idx in range(brace_start, len(content)):
                if content[idx] == '{':
                    depth += 1
                elif content[idx] == '}':
                    depth -= 1
                    if depth == 0:
                        end = idx + 1
                        break
            method_body = content[m.start():end]
            if 'ViolatedAssumptionAnswer' in method_body or 'mock(' in method_body:
                content = content[:m.start()] + content[end:]
                removed_mock += 1
            else:
                content = content[:m.start()] + content[m.start():end].replace('@Test', '@_KEEP_', 1) + content[end:]

        content = content.replace('@_KEEP_', '@Test')

        if removed_mock:
            print(f"[EvoSuite] Removed {removed_mock} Mockito-dependent test methods")

        # --- Pass 2: strip all EvoSuite imports and annotations ---
        content = re.sub(r'import\s+org\.evosuite\.runtime\.\w+;\n', '', content)
        content = re.sub(r'import\s+static\s+org\.evosuite\.runtime\.\w+\.\*;\n', '', content)
        content = re.sub(r'import\s+static\s+org\.evosuite\.shaded\.[^;]+;\n', '', content)
        content = re.sub(r'@RunWith\(EvoRunner\.class\)\s*', '', content)
        content = re.sub(r'@EvoRunnerParameters\([^)]*\)\s*\n?', '', content)
        content = re.sub(
            r'(public\s+class\s+\w+)\s+extends\s+\w+_scaffolding\b',
            r'\1', content
        )

        # --- Pass 3: remove verifyException(...) calls ---
        # The try-catch already validates the exception type; this call is just
        # an EvoSuite utility that checks the stack trace origin.
        content = re.sub(r'\s*verifyException\([^)]*\);\n', '\n', content)

        with open(test_file, 'w') as f:
            f.write(content)

        print(f"[EvoSuite] Sanitized test for standalone JUnit 4: {test_file}")

    def _find_test_file(self, test_dir):
        if not os.path.exists(test_dir):
            return None
        for root, dirs, files in os.walk(test_dir):
            for f in files:
                if f.endswith("_ESTest.java"):
                    return os.path.join(root, f)
        return None

    def get_runtime_jar(self):
        return self.runtime_jar if os.path.exists(self.runtime_jar) else None
