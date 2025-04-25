from numpy.lib.utils import source

from llm_agents.llm_agent import LLMAgent
from langchain.schema import SystemMessage
from llm_agents.unit_test_generator import UnitTestGenerator
from utils.function_utils import *
from utils.java_executor import *
import random
from config.config import *

## Mutation mapping
MUTATION_MAPPING = {
    # Boolean mutations
    "true": ["false"],
    "false": ["true"],
    "&&": ["||"],
    "||": ["&&"],
    "!": [""],  # Remove negation
    "==": ["!="],
    "!=": ["=="],

    # Arithmetic mutations
    "+": ["-", "*", "/"],
    "-": ["+", "*", "/"],
    "*": ["/", "+", "-"],
    "/": ["*", "+", "-"],

    # Increment/decrement mutations
    "++": ["--"],
    "--": ["++"],

    # Relational operator mutations
    "<": ["<=", ">"],
    "<=": ["<", ">="],
    ">": [">=", "<"],
    ">=": [">", "<="],

    # Return statement mutations
    "return true": ["return false"],
    "return false": ["return true"],
    "return 0": ["return 1", "return -1"],
    "return 1": ["return 0", "return -1"],

    # Assignment mutations
    "=": ["+=", "-=", "*=", "/="],

    # Loop boundary mutations
    "for (": ["while ("],  # Convert loops
    "while (": ["for ("],

    # Null reference mutations
    "null": ["new Object()"],

    # Method call mutations
    ".size()": [".size() - 1", ".size() + 1"],  # List size edge cases
    ".get(": [".get(0)", ".get(1)"],  # List index variations
}

class MutationAssertionGenerator(LLMAgent):
    def __init__(self,api_key, model, temperature, unit_test_path, source_code_path):
        super().__init__(api_key, model, temperature)
        self.unit_test_path = unit_test_path
        self.source_code_path = source_code_path
        self.unit_test_java_executor = JavaExecutor(java_file_path=unit_test_path)
        self.system_prompt = open(os.path.abspath(os.path.join("prompts", "mutation_assertion_generator", "system_prompt.txt")),'r').read()
        self.input_prompt = open(os.path.abspath(os.path.join("prompts", "mutation_assertion_generator", "input_prompt.txt")),'r').read()
        self.mutation_mapping = MUTATION_MAPPING



    def exact_operator_regex(self,pattern):
        """
        Builds a regex pattern that ensures only the exact operator is matched, not a substring of a larger operator.
        """
        escaped = re.escape(pattern)
        if pattern in ["++", "--", "==", "!=", "<=", ">="]:
            return rf'(?<!\w){escaped}(?!\w)'
        elif pattern in ["+", "-", "*", "/", "<", ">"]:
            return rf'(?<![\w{escaped}]){escaped}(?![\w{escaped}=])'
        elif pattern == "=":
            return rf'(?<![!=<>]){escaped}(?![=])'
        elif pattern == "!":
            return rf'(?<![=!]){escaped}(?![=])'
        else:
            return escaped  # fallback

    def get_assertion_injection(self, session_id: str) -> str:

        # Compose the prompt including the system message, long-term memory, and user input
        system_message =SystemMessage(content=f"{self.system_prompt}")

        # Get or create the message history for the session
        history = self.get_chat_history(session_id)

        # Add system message and user prompt to the conversation
        messages = [system_message] + history.messages
        response = self.chat_model(messages)

        # Update the session chat history and long-term memory
        history.add_assistant_message(response.content)

        return response.content

    def apply_mutation(self, java_function):
        """
        Applies a single random mutation from the mutation mapping to a Java function.

        Args:
            java_function (str): The original Java function as a string.

        Returns:
            tuple: (mutated_function, mutation_description)
        """
        mutated_function = java_function  # Keep original function structure
        applied_mutation = None

        # Shuffle mutation keys to apply a random mutation
        mutation_keys = list(self.mutation_mapping.keys())
        random.shuffle(mutation_keys)  # Ensures random selection

        for pattern in mutation_keys:
            regex_pattern = self.exact_operator_regex(pattern)
            if re.search(regex_pattern, mutated_function):
                possible_mutations = self.mutation_mapping[pattern]
                chosen_mutation = random.choice(possible_mutations)
                mutated_function, num_subs = re.subn(regex_pattern, chosen_mutation, mutated_function, count=1)
                if num_subs > 0:
                    applied_mutation = f"Replaced `{pattern}` with `{chosen_mutation}`"
                    break

        return mutated_function, applied_mutation

    def mutation_assertion_generation(self):
        source_code = read_java_file_as_string(self.source_code_path)
        unit_test = read_java_file_as_string(self.unit_test_path)
        source_code_functions = extract_public_methods(source_code)
        num_functions = len(source_code_functions)

        for function_name in source_code_functions:
            # Determine if we should mutate this function (1/|T| chance)
            if random.random() > 1 / num_functions:
                continue  # Skip mutation for this function

            function = extract_java_function(source_code, function_name)
            mutated_function, applied_mutation = self.apply_mutation(function)

            if not applied_mutation:
                continue  # No mutation was actually applied

            modified_source_code = replace_java_function(source_code, function_name, mutated_function)
            save_test_suite(modified_source_code, self.source_code_path)
            executor = JavaExecutor(java_file_path=self.source_code_path)
            executor.compile_java()
            test_ran, output = self.unit_test_java_executor.run_java()

            while test_ran and applied_mutation:
                self.update_long_term_memory('session1',
                                             self.input_prompt.format(function, mutated_function, unit_test))
                modified_test = self.get_assertion_injection('session1')
                test_name = extract_public_methods(modified_test)[0]
                modified_unit_test = replace_java_function(unit_test, test_name, modified_test)
                save_test_suite(modified_unit_test, self.unit_test_path)
                test_ran, output = self.unit_test_java_executor.run_java()

            # Restore original source code after mutation round
            save_test_suite(source_code, self.source_code_path)


