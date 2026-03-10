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

    @staticmethod
    def _need_enhancement(coverage_metrics: dict, missed_branches: dict) -> bool:
        """
        Determines if enhancement is needed based on coverage metrics and missed branches.
        :param coverage_metrics: The coverage metrics
        :param missed_branches: The missed branches
        :return: True if enhancement is needed, False otherwise
        """
        tested_class_path = list(coverage_metrics.keys())[0]
        for method, metrics in coverage_metrics[tested_class_path].items():
            if metrics.get("branch_coverage", 100) < 75 or metrics.get("line_coverage", 100) < 75:
                return True

        if missed_branches:
            return True

        return False

    async def generation_repair_loop(self, coverage_metrics: dict, missed_branches: dict, iterations: int = 4,
                                     thread_number: int = None, output_path: Path = Path.cwd()):
        """
        Generates a coverage enhancement for the given class.
        :param coverage_metrics: The coverage metrics
        :param missed_branches: The missed branches
        :param iterations: The number of iterations
        :param thread_number: The thread number
        :param output_path: The output path for the generated test
        :return: The coverage enhancement
        """
        if not self._need_enhancement(coverage_metrics, missed_branches):
            self.logger.debug("No enhancement needed based on coverage metrics and missed branches.")
            return False

        java_code = self._read_and_clean_java_code(self.java_file_path)
        project_root = extract_project_name(self.java_file_path)
        self.update_long_term_memory(self.session_id, INPUT_PROMPT.format(java_code, coverage_metrics, missed_branches))
        current_test_suite = await self.get_unit_test_for_class(session_id=self.session_id)
        base_dir = self._prepare_test_path_and_save(current_test_suite=current_test_suite,
                                                    java_file_path=self.java_file_path,
                                                    output_path=output_path,
                                                    project_id=project_root.stem,
                                                    thread_number=thread_number,
                                                    test_extension="Enhanced")

        test_file_path = base_dir / f"{self.java_file_path.stem}EnhancedTest.java"
        ## Generation repair loop
        executor = JavaExecutor(test_file_path)
        for iteration in range(iterations):
            validated_test_suite = await self._check_syntax_and_fix(executor=executor,
                                                                    current_test_suite=current_test_suite,
                                                                    test_file_path=test_file_path)
            compiled_successfully, current_test_suite = await self._compile_test_and_fix(executor=executor,
                                                                                         current_test_suite=validated_test_suite,
                                                                                         java_file_path=self.java_file_path,
                                                                                         test_file_path=test_file_path)

            if not compiled_successfully:
                continue

            executed_successfully = await self._execute_code_and_fix_errors(executor=executor,
                                                                            current_test_suite=current_test_suite,
                                                                            test_file_path=test_file_path)
            if executed_successfully:
                return True

            # Fallback: remove failing tests one by one (keep at least 1)
        return self._remove_failing_tests_one_by_one(executor=executor,
                                                     current_test_suite=current_test_suite,
                                                     test_file_path=test_file_path)
