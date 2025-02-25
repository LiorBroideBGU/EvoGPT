from llm_agents.llm_agent import LLMAgent
from langchain.schema import SystemMessage
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

    def generation_repair_loop(self,coverage_metrics, missed_branches, iterations=4):
        java_code = read_java_file_as_string(self.java_file_path)
        java_code = clean_java_code(java_code)
        project_id = extract_project_name(self.java_file_path).split("\\")[-1]
        java_class_name = self.java_file_path.split("\\")[-1].split(".")[0]
        self.update_long_term_memory('session1',self.input_prompt.format(java_code, coverage_metrics, missed_branches))
        current_test_suite = self.get_unit_test_for_class(session_id='session1')
        test_file_path = os.path.abspath(os.path.join("results", "unit_tests", project_id,java_class_name,'javafiles', f"{java_class_name}EnhancedTest.java"))
        os.makedirs(os.path.dirname(test_file_path), exist_ok=True)
        save_test_suite(current_test_suite, test_file_path)

        ## Generation repair loop
        success, output = False, None
        executor = JavaExecutor(test_file_path)
        for iteration in range(iterations):
            try:
                executor.check_java_code_syntax()
            except SyntaxError as e:
                success, output = False, e
                self.update_long_term_memory('session1',self.syntax_error_prompt.format(output))
                current_test_suite = self.get_unit_test_for_class(session_id='session1')

            success, output = executor.compile_java()
            if not success:
                unimport_classes = get_class_imports(extract_project_name(self.java_file_path), output)
                if unimport_classes:
                    current_test_suite = add_imports(unimport_classes, current_test_suite)
                    self.edit_history_response('session1', current_test_suite)
                    save_test_suite(current_test_suite, test_file_path)

            success, output = executor.run_java()
            if not success:
                self.update_long_term_memory('session1', self.repair_prompt.format(output))
                current_test_suite = self.get_unit_test_for_class(session_id='session1')
                save_test_suite(current_test_suite, test_file_path)

            else:
                return

        current_test_suite = remove_junit_tests(current_test_suite, output)
        save_test_suite(current_test_suite, test_file_path)

