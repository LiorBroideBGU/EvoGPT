from llm_agents.llm_agent import LLMAgent
from langchain.schema import SystemMessage
from utils.function_utils import *
from utils.java_executor import *


class UnitTestGenerator(LLMAgent):
    def __init__(self, api_key, model, temperature):
        super().__init__(api_key, model, temperature)
        self.input_prompt = open(os.path.abspath(os.path.join("prompts", "unit_test_generator", "input_prompt.txt")),
                                 'r').read()
        self.system_prompt = open(os.path.abspath(os.path.join("prompts", "unit_test_generator", "system_prompt.txt")),
                                  'r').read()
        self.repair_prompt = open(os.path.abspath(os.path.join("prompts", "unit_test_generator", "repair_prompt.txt")),
                                  'r').read()
        self.syntax_error_prompt = open(
            os.path.abspath(os.path.join("prompts", "unit_test_generator", "syntax_error_prompt.txt")), 'r').read()

    def get_unit_test_for_class(self, session_id: str) -> str:
        # Retrieve long-term memory specific to the session
        long_term_memory = self.get_long_term_memory(session_id)

        # Compose the prompt including the system message, long-term memory, and user input
        system_message = SystemMessage(content=f"{self.system_prompt}")

        # Get or create the message history for the session
        history = self.get_chat_history(session_id)

        # Add system message and user prompt to the conversation
        messages = [system_message] + history.messages
        response = self.chat_model(messages)

        # Update the session chat history and long-term memory
        history.add_assistant_message(response.content)

        return response.content

    def generation_repair_loop(self, java_file_path, project_id, iterations=4, thread_number=None):
        java_code = read_java_file_as_string(java_file_path)
        java_code = clean_java_code(java_code)
        java_class_name = java_file_path.split("\\")[-1].split(".")[0]
        self.update_long_term_memory('session1', self.input_prompt.format(java_code))
        current_test_suite = self.get_unit_test_for_class(session_id='session1')
        test_file_path = os.path.abspath(
            os.path.join("results", "unit_tests", project_id, java_class_name, str(thread_number), 'javafiles',
                         f"{java_class_name}Test.java")) if thread_number else os.path.abspath(
            os.path.join("results", "unit_tests", project_id, java_class_name, 'javafiles',
                         f"{java_class_name}Test.java"))
        os.makedirs(os.path.dirname(test_file_path), exist_ok=True)
        save_test_suite(current_test_suite, test_file_path)
        if thread_number:
            save_test_suite(java_code, os.path.abspath(
                os.path.join("results", "unit_tests", project_id, java_class_name,str(thread_number), 'javafiles', f"{java_class_name}.java")))
        else:
            save_test_suite(java_code, os.path.abspath(
                os.path.join("results", "unit_tests", project_id, java_class_name, 'javafiles', f"{java_class_name}.java")))

        ## Generation repair loop
        success, output = False, None
        executor = JavaExecutor(test_file_path)
        for iteration in range(iterations):
            try:
                executor.check_java_code_syntax()
            except SyntaxError as e:
                success, output = False, e
                self.update_long_term_memory('session1', self.syntax_error_prompt.format(output))
                current_test_suite = self.get_unit_test_for_class(session_id='session1')

            success, output = executor.compile_java()
            if not success:
                unimport_classes = get_class_imports(extract_project_name(java_file_path), output)
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
        for i in range(6):
            success, output = executor.run_java()
            if not success:
                current_test_suite = remove_junit_tests(current_test_suite, output)
                save_test_suite(current_test_suite, test_file_path)
            else:
                break