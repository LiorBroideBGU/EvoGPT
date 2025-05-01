from numpy.lib.utils import source

from llm_agents.llm_agent import LLMAgent
from langchain.schema import SystemMessage
from llm_agents.unit_test_generator import UnitTestGenerator
from utils.function_utils import *
from utils.java_executor import *
import random
from config.config import *



class MutationAssertionGenerator(LLMAgent):
    def __init__(self,api_key, model, temperature, unit_test_path, source_code_path):
        super().__init__(api_key, model, temperature)
        self.unit_test_path = unit_test_path
        self.source_code_path = source_code_path
        self.unit_test_java_executor = JavaExecutor(java_file_path=unit_test_path)
        self.system_prompt = open(os.path.abspath(os.path.join("prompts", "mutation_assertion_generator", "system_prompt.txt")),'r').read()
        self.input_prompt = open(os.path.abspath(os.path.join("prompts", "mutation_assertion_generator", "input_prompt.txt")),'r').read()

    def extract_test_methods(self, code: str):
        method_pattern = re.compile(r'@Test\s+public\s+void\s+(\w+)\s*\([^)]*\)\s*\{', re.MULTILINE)
        methods = []
        for match in method_pattern.finditer(code):
            start = match.start()
            braces = 0
            end = start
            for i in range(start, len(code)):
                if code[i] == '{':
                    braces += 1
                elif code[i] == '}':
                    braces -= 1
                    if braces == 0:
                        end = i + 1
                        break
            methods.append(code[start:end])
        return methods

    def get_imports(self,code: str):
        return set(re.findall(r'^import\s+.*?;', code, re.MULTILINE))

    def get_fields(self,code: str):
        pattern = re.compile(
            r'^\s*(private|protected|public)\s+[\w\<\>\[\]]+\s+\w+\s*;',
            re.MULTILINE
        )
        fields = []
        for match in pattern.finditer(code):
            field_line = match.group(0).strip()
            normalized = ' '.join(field_line.split())
            fields.append(normalized)
        return fields

    async def get_model_response(self):
        # Retrieve long-term memory specific to the session
        long_term_memory = self.get_long_term_memory('session1')

        # Compose the prompt including the system message, long-term memory, and user input
        system_message = SystemMessage(content=f"{self.system_prompt}")

        # Get or create the message history for the session
        history = self.get_chat_history('session1')

        # Add system message and user prompt to the conversation
        messages = [system_message] + history.messages
        response =  self.chat_model(messages)

        # Update the session chat history and long-term memory
        await history.add_assistant_message(response.content)

        return response.content

    def replace_test_method(self,original_code: str, new_method_code: str) -> str:    # Extract method name from new method
        method_name_match = re.search(r'@Test\s+public\s+void\s+(\w+)\s*\(', new_method_code)
        if not method_name_match:
            raise ValueError("Could not extract method name from new test method.")

        method_name = method_name_match.group(1)

        # Find the start and end indices of the old method in the original code
        method_pattern = re.compile(
            rf'@Test\s+public\s+void\s+{method_name}\s*\([^)]*\)\s*\{{',
            re.MULTILINE
        )
        match = method_pattern.search(original_code)
        if not match:
            raise ValueError(f"No method named '{method_name}' found in original code.")

        start_index = match.start()

        # Now find where this method ends (match balanced braces)
        brace_count = 0
        end_index = start_index
        for i in range(start_index, len(original_code)):
            if original_code[i] == '{':
                brace_count += 1
            elif original_code[i] == '}':
                brace_count -= 1
                if brace_count == 0:
                    end_index = i + 1
                    break

        # Slice and replace only that method
        updated_code = original_code[:start_index] + new_method_code.strip() + original_code[end_index:]
        return updated_code

    async def assertion_generation(self):
        old_java_source_code = read_java_file_as_string(self.source_code_path)
        old_unit_test = read_java_file_as_string(self.unit_test_path)
        test_methods = self.extract_test_methods(old_unit_test)
        num_of_test_methods = len(test_methods)
        for test_method in test_methods:
            old_unit_test = read_java_file_as_string(self.unit_test_path)
            if random.random() <= 1/num_of_test_methods:
                fields = self.get_fields(old_java_source_code)
                imports = self.get_imports(old_java_source_code)
                self.update_long_term_memory('session1', self.input_prompt.format(test_method, old_java_source_code, f"Fields: {fields}, Imports: {imports}"))
                new_test_method =  await self.get_model_response()
                new_unit_test = self.replace_test_method(old_unit_test, new_test_method)
                save_test_suite(new_unit_test, self.unit_test_path)
                result, stacktrace = self.unit_test_java_executor.run_java()
                if not result:
                    save_test_suite(old_unit_test, self.unit_test_path)





