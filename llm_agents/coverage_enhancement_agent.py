
from llm_agents.unit_test_generator import UnitTestGenerator
from utils.function_utils import *
from utils.java_executor import *

class CoverageEnhancementAgent(UnitTestGenerator):
    def __init__(self, api_key,model, temperature, java_file_path):
        super().__init__(api_key, model, temperature)
        self.input_prompt = open(os.path.abspath(os.path.join("prompts", "coverage_enhancement_generator", "input_prompt.txt")),'r').read()
        self.system_prompt = open(os.path.abspath(os.path.join("prompts", "coverage_enhancement_generator", "system_prompt.txt")),'r').read()
        self.repair_prompt = open(os.path.abspath(os.path.join("prompts", "coverage_enhancement_generator", "repair_prompt.txt")),'r').read()
        self.syntax_error_prompt = open(os.path.abspath(os.path.join("prompts", "coverage_enhancement_generator", "syntax_error_prompt.txt")),'r').read()
        self.java_file_path = java_file_path

    async def generation_repair_loop(self,coverage_metrics, missed_branches, iterations=4, thread_number=None):
        java_code = read_java_file_as_string(self.java_file_path)
        java_code = clean_java_code(java_code)
        # Use os.path.basename for cross-platform compatibility
        project_id = os.path.basename(extract_project_name(self.java_file_path))
        java_class_name = os.path.basename(self.java_file_path).split(".")[0]
        self.update_long_term_memory('session1',self.input_prompt.format(java_code, coverage_metrics, missed_branches))
        current_test_suite = await self.get_unit_test_for_class(session_id='session1')
        test_file_path = os.path.abspath(os.path.join("results", "unit_tests", project_id,java_class_name,str(thread_number),'javafiles', f"{java_class_name}EnhancedTest.java")) if thread_number else os.path.abspath(os.path.join("results", "unit_tests", project_id,java_class_name,'javafiles', f"{java_class_name}EnhancedTest.java"))
        os.makedirs(os.path.dirname(test_file_path), exist_ok=True)
        save_test_suite(current_test_suite, test_file_path)

        ## Generation repair loop
        executor = JavaExecutor(test_file_path)
        for iteration in range(iterations):
            # --- Syntax check ---
            try:
                executor.check_java_code_syntax()
            except SyntaxError as e:
                self.update_long_term_memory('session1', self.syntax_error_prompt.format(e))
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
                self.update_long_term_memory('session1', self.repair_prompt.format(compile_err))
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
