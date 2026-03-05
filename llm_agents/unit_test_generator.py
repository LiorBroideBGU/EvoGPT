from llm_agents.llm_agent import LLMAgent
from langchain_core.messages import SystemMessage
from utils.function_utils import *
from utils.java_executor import *
from utils.dataset_utils import *
from pathlib import Path
from prompts.unit_test_generator_prompts import *

SYSTEM_PROMPT_MAP = {
    0.3: SYSTEM_PROMPT_DEFAULT,
    0.6: SYSTEM_PROMPT_ASSERTION_HEAVY,
    0.8: SYSTEM_PROMPT_BUG_DETECTOR,
    0.5: SYSTEM_PROMPT_EDGE_CASE_EXPLORER,
    0.4: SYSTEM_PROMPT_HIGH_COVERAGE,
}

class UnitTestGenerator(LLMAgent):
    """
    Unit Test Generator agent for evolutionary unit test generation.
    """

    def __init__(self, api_key: str, model: str, temperature: float):
        super().__init__(api_key, model, temperature)
        self.system_prompt = SYSTEM_PROMPT_MAP[temperature]

    async def get_unit_test_for_class(self, session_id: str) -> str:
        """
        Gets a unit test for the given class.
        :param session_id: The session id
        :return: The unit test
        """
        self.logger.debug(f"Getting unit test for class: {session_id}")
        system_message = SystemMessage(content=f"{self.system_prompt}")
        history = self.get_chat_history(session_id)
        messages = [system_message] + history.messages
        response = await self.chat_model.ainvoke(messages)
        await history.add_assistant_message(response.content)
        return response.content

    async def generation_repair_loop(self, java_file_path: Path, project_id: str, iterations: int = 4, thread_number: int = None):
        """
        Generates a unit test for the given class.
        :param java_file_path: The path to the Java file
        :param project_id: The project id
        :param iterations: The number of iterations
        :param thread_number: The thread number
        :return: The unit test
        """
        self.logger.debug(f"Generating unit test for class: {java_file_path}")
        java_code = read_java_file_as_string(java_file_path)
        java_code = clean_java_code(java_code)
        public_methods_list = get_public_method_signatures(java_code)
        self.logger.debug(f"Public methods list: {public_methods_list}")
        java_class_name = java_file_path.stem
        self.update_long_term_memory('session1', INPUT_PROMPT.format(public_methods_list, java_code))
        self.logger.debug(f"Updating long-term memory for session: session1")
        current_test_suite = await self.get_unit_test_for_class(session_id='session1')
        self.logger.debug(f"Current test suite: {current_test_suite}")

        if thread_number:
            base_dir = Path("results", "unit_tests", project_id, java_class_name, str(thread_number), "javafiles")
        else:
            base_dir = Path("results", "unit_tests", project_id, java_class_name, "javafiles")

        test_file_path = base_dir / f"{java_class_name}Test.java"
        source_file_path = base_dir / f"{java_class_name}.java"

        self.logger.debug(f"Test file path: {test_file_path}")
        base_dir.mkdir(parents=True, exist_ok=True)

        self.logger.debug(f"Saving test suite to: {test_file_path}")
        save_test_suite(current_test_suite, test_file_path)
        self.logger.debug(f"Saving Java code to: {source_file_path}")
        save_test_suite(java_code, source_file_path)

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
                unimport_classes = get_class_imports(extract_project_name(java_file_path), compile_err)
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

            current_test_suite = fix_unit_test(current_test_suite, run_err)
            save_test_suite(current_test_suite, test_file_path)

            ran, run_err = executor.run_java()
            if ran:
                return

            self.update_long_term_memory('session1', REPAIR_PROMPT.format(run_err))
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