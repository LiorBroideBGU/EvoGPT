
from llm_agents.unit_test_generator import UnitTestGenerator
from utils.function_utils import *
from utils.java_executor import *
from pathlib import Path
from prompts.coverage_enhancement_generator_prompts import *

class CoverageEnhancementAgent(UnitTestGenerator):
    """
    Coverage Enhancement Agent for evolutionary unit test generation.
    """

    def __init__(self, api_key: str, model: str, temperature: float, java_file_path: Path):
        super().__init__(api_key, model, temperature)
        self.java_file_path = java_file_path

    async def generation_repair_loop(self,coverage_metrics: dict, missed_branches: list, iterations: int = 4, thread_number: int = None):
        """
        Generates a coverage enhancement for the given class.
        :param coverage_metrics: The coverage metrics
        :param missed_branches: The missed branches
        :param iterations: The number of iterations
        :param thread_number: The thread number
        :return: The coverage enhancement
        """
        java_code = read_java_file_as_string(self.java_file_path)
        java_code = clean_java_code(java_code)
        project_root = extract_project_name(self.java_file_path)
        project_id = project_root.name
        java_class_name = self.java_file_path.stem
        self.update_long_term_memory('session1', INPUT_PROMPT.format(java_code, coverage_metrics, missed_branches))
        current_test_suite = await self.get_unit_test_for_class(session_id='session1')
        if thread_number:
            test_file_path = Path("results", "unit_tests", project_id, java_class_name, str(thread_number), "javafiles", f"{java_class_name}EnhancedTest.java")
        else:
            test_file_path = Path("results", "unit_tests", project_id, java_class_name, "javafiles", f"{java_class_name}EnhancedTest.java")
        test_file_path.parent.mkdir(parents=True, exist_ok=True)
        save_test_suite(current_test_suite, test_file_path)

        ## Generation repair loop
        executor = JavaExecutor(test_file_path)
        for iteration in range(iterations):
            # --- Syntax check ---
            try:
                executor.check_java_code_syntax()
            except SyntaxError as e:
                self.update_long_term_memory('session1', SYNTAX_ERROR_PROMPT.format(e))
                current_test_suite = await self.get_unit_test_for_class(session_id='session1')
                save_test_suite(current_test_suite, test_file_path)

            # --- Compilation ---
            compiled, compile_err = executor.compile_java()
            if not compiled:
                unimport_classes = get_class_imports(extract_project_name(self.java_file_path), compile_err)
                if unimport_classes:
                    current_test_suite = add_imports(unimport_classes, current_test_suite)
                    await self.edit_history_response('session1', current_test_suite)
                    save_test_suite(current_test_suite, test_file_path)
                    compiled, compile_err = executor.compile_java()

            if not compiled:
                self.update_long_term_memory('session1', REPAIR_PROMPT.format(compile_err))
                current_test_suite = await self.get_unit_test_for_class(session_id='session1')
                save_test_suite(current_test_suite, test_file_path)
                continue

            # --- Runtime ---
            ran, run_err = executor.run_java()
            if ran:
                return

            self.update_long_term_memory('session1', self.repair_prompt.format(run_err))
            current_test_suite = await self.get_unit_test_for_class(session_id='session1')
            save_test_suite(current_test_suite, test_file_path)

        # Fallback: remove failing tests one by one (keep at least 1)
        for _ in range(6):
            ran, run_err = executor.run_java()
            if ran:
                break
            cleaned = remove_junit_tests(current_test_suite, run_err)
            if cleaned == current_test_suite:
                break
            has_test = re.search(r'@Test\s+public\s+void\s+\w+', cleaned)
            if not has_test:
                break
            current_test_suite = cleaned
            save_test_suite(current_test_suite, test_file_path)
