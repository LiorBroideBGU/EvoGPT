from llm_agents.llm_agent import LLMAgent
from langchain_core.messages import SystemMessage
from utils.function_utils import *
from utils.java_executor import *
from utils.dataset_utils import *
from pathlib import Path
from prompts.unit_test_generator_prompts import *
from utils.java_executor import JavaExecutor

NUM_FAILING_TESTS = 6

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

    session_id = 'session1'

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

    @staticmethod
    def _read_and_clean_java_code(java_file_path: Path) -> str:
        """
        Reads and cleans the Java code from the given file path.
        :param java_file_path: The path to the Java file
        :return: The cleaned Java code
        """
        java_code = clean_java_code(read_java_file_as_string(java_file_path))
        return java_code

    def _prepare_test_path_and_save(self, current_test_suite: str, java_file_path: Path, output_path: Path,
                                    project_id: str, thread_number: int = None, test_extension: str = "") -> Path:
        """
        Prepares the test file path.
        :param output_path: The base output path
        :param project_id: The project id
        :param thread_number: The thread number
        :return: The test file path
        """
        java_class_name = java_file_path.stem
        if thread_number:
            base_dir = output_path / "unit_tests" / project_id / java_class_name / str(thread_number) / "javafiles"
        else:
            base_dir = output_path / "unit_tests" / project_id / java_class_name / "javafiles"

        base_dir.mkdir(parents=True, exist_ok=True)
        test_file_path = base_dir / f"{java_class_name}{test_extension}Test.java"
        self.logger.debug(f"Saving test suite to: {test_file_path}")
        save_code(current_test_suite, test_file_path)

        return base_dir

    async def _execute_code_and_fix_errors(self, executor: JavaExecutor, current_test_suite: str,
                                           test_file_path: Path) -> bool:
        """
        Executes the code and fixes errors if any.
        :param executor: The JavaExecutor instance
        :param current_test_suite: The current test suite code
        :param test_file_path: The path to the test file
        :return: Whether the code ran successfully after fixing errors
        """
        # --- Runtime ---
        executed_successfully, run_err = executor.run_java()
        if executed_successfully:
            return executed_successfully

        fixed_unit_test = fix_unit_test(current_test_suite, run_err)
        save_code(fixed_unit_test, test_file_path)
        executed_successfully, run_err = executor.run_java()
        if executed_successfully:
            return executed_successfully

        self.update_long_term_memory(self.session_id, REPAIR_PROMPT.format(run_err))
        fixed_unit_test = await self.get_unit_test_for_class(session_id=self.session_id)
        save_code(fixed_unit_test, test_file_path)
        return False

    async def _check_syntax_and_fix(self, executor: JavaExecutor, current_test_suite: str, test_file_path: Path) -> str:
        """

        """
        validated_test_suite = current_test_suite
        try:
            executor.check_java_code_syntax()
            return validated_test_suite

        except SyntaxError as e:
            self.update_long_term_memory(self.session_id, SYNTAX_ERROR_PROMPT.format(e))
            validated_test_suite = await self.get_unit_test_for_class(session_id=self.session_id)
            save_code(validated_test_suite, test_file_path)

        return validated_test_suite

    async def _compile_test_and_fix(
            self,
            executor: JavaExecutor,
            current_test_suite: str,
            java_file_path: Path,
            test_file_path: Path) -> tuple[bool, str]:
        """

        """
        repaired_test_suite = current_test_suite
        compiled_successfully, compilation_err = executor.compile_java()
        if compiled_successfully:
            return compiled_successfully

        self.logger.debug(f"Performing test fix heuristic to fix possibly missing imports.")
        unimport_classes = get_class_imports(extract_project_name(java_file_path), compilation_err)
        if unimport_classes:
            self.logger.debug(f"Unimport classes: {unimport_classes}")
            repaired_test_suite = add_imports(unimport_classes, repaired_test_suite)
            await self.edit_history_response(self.session_id, repaired_test_suite)
            save_code(repaired_test_suite, test_file_path)
            compiled_successfully, compilation_err = executor.compile_java()

        # If still not compiled successfully, update long-term memory with the compilation error and get a new test suite
        if not compiled_successfully:
            self.update_long_term_memory(self.session_id, REPAIR_PROMPT.format(compilation_err))
            repaired_test_suite = await self.get_unit_test_for_class(session_id=self.session_id)
            save_code(repaired_test_suite, test_file_path)

        return compiled_successfully, repaired_test_suite

    def _remove_failing_tests_one_by_one(self, executor: JavaExecutor, current_test_suite: str,
                                         test_file_path: Path) -> bool:
        """
        Removes failing tests one by one as a fallback mechanism.
        :param executor: The JavaExecutor instance
        :param current_test_suite: The current test suite code
        :param test_file_path: The path to the test file
        :return: Whether the code ran successfully after removing failing tests
        """
        cleaned_test_suite = current_test_suite
        for _ in range(NUM_FAILING_TESTS):
            run_successfully, run_err = executor.run_java()
            if run_successfully:
                return run_successfully

            cleaned = remove_junit_tests(cleaned_test_suite, run_err)
            if cleaned == current_test_suite:
                break

            has_test = re.search(r'@Test\s+public\s+void\s+\w+', cleaned)
            if not has_test:
                break

            cleaned_test_suite = cleaned
            save_code(cleaned_test_suite, test_file_path)

        return False

    async def generation_repair_loop(self, java_file_path: Path, project_id: str, iterations: int = 4,
                                     thread_number: int = None, output_path: Path = Path.cwd()) -> bool:
        """
        Generates a unit test for the given class.
        :param java_file_path: The path to the Java file
        :param project_id: The project id
        :param iterations: The number of iterations
        :param thread_number: The thread number
        :param output_path: The output path for the generated test
        :return: The unit test
        """
        self.logger.debug(f"Generating unit test for class: {java_file_path}")
        java_code = self._read_and_clean_java_code(java_file_path)
        public_methods_list = get_public_method_signatures(java_code)
        self.logger.debug(f"Public methods list: {public_methods_list}")
        self.update_long_term_memory(self.session_id, INPUT_PROMPT.format(public_methods_list, java_code))
        self.logger.debug(f"Updating long-term memory for session: {self.session_id}")
        current_test_suite = await self.get_unit_test_for_class(session_id=self.session_id)
        base_dir = self._prepare_test_path_and_save(current_test_suite=current_test_suite,
                                                    java_file_path=java_file_path,
                                                    output_path=output_path,
                                                    project_id=project_id,
                                                    thread_number=thread_number)

        test_file_path = base_dir / f"{java_file_path.stem}Test.java"
        source_file_path = base_dir / f"{java_file_path.stem}.java"
        self.logger.debug(f"Saving Java code to: {source_file_path}")
        save_code(java_code, source_file_path)
        ## Generation repair loop
        executor = JavaExecutor(test_file_path)
        for iteration in range(iterations):
            validated_test_suite = await self._check_syntax_and_fix(executor=executor,
                                                                    current_test_suite=current_test_suite,
                                                                    test_file_path=test_file_path)
            compiled_successfully, current_test_suite = await self._compile_test_and_fix(executor=executor,
                                                                                         current_test_suite=validated_test_suite,
                                                                                         java_file_path=java_file_path,
                                                                                         test_file_path=test_file_path)
            if not compiled_successfully:
                continue

            executed_successfully = await self._execute_code_and_fix_errors(executor=executor,
                                                                            current_test_suite=current_test_suite,
                                                                            test_file_path=test_file_path)
            if executed_successfully:
                return True

        # Fallback: remove failing tests one by one (keep at least 1)
        return self._remove_failing_tests_one_by_one(executor=executor, current_test_suite=current_test_suite,
                                                     test_file_path=test_file_path)
