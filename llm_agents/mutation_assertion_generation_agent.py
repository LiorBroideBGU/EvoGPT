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



    def get_assertion_injection(self, session_id: str) -> str:
        # Retrieve long-term memory specific to the session
        long_term_memory = self.get_long_term_memory(session_id)

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
        # random.shuffle(mutation_keys)  # Ensures random selection

        for pattern in mutation_keys:
            # Ensure correct boundaries for special cases like '=' vs '==', and '!' vs '!='
            if pattern in ["=", "!"]:
                regex_pattern = rf"(?<![=!]){re.escape(pattern)}(?![=])"
            else:
                regex_pattern = re.escape(pattern)

            if re.search(regex_pattern, mutated_function):  # Check if pattern exists
                possible_mutations = self.mutation_mapping[pattern]
                chosen_mutation = random.choice(possible_mutations)  # Randomly pick a mutation

                # Apply mutation correctly using regex
                mutated_function, num_subs = re.subn(regex_pattern, chosen_mutation, mutated_function, count=1)

                if num_subs > 0:  # Ensure mutation occurred
                    applied_mutation = f"Replaced `{pattern}` with `{chosen_mutation}`"
                    break  # Apply only one mutation per function

        return mutated_function, applied_mutation

    def mutation_generation(self):
        source_code = read_java_file_as_string(self.source_code_path)
        unit_test = read_java_file_as_string(self.unit_test_path)
        source_code_functions = extract_public_methods(source_code)
        for function_name in source_code_functions:
            function = extract_java_function(source_code, function_name)
            mutated_function, applied_mutation = self.apply_mutation(function)
            modified_source_code = replace_java_function(source_code, function_name, mutated_function)
            save_test_suite(modified_source_code, self.source_code_path)
            executor = JavaExecutor(java_file_path=self.source_code_path)
            executor.compile_java()
            test_ran, output = self.unit_test_java_executor.run_java()
            while test_ran and applied_mutation:
                print(applied_mutation)
                self.update_long_term_memory('session1',
                                             self.input_prompt.format(function, mutated_function,unit_test))
                modified_test = self.get_assertion_injection('session1')
                test_name = extract_public_methods(modified_test)[0]
                modified_unit_test = replace_java_function(unit_test,test_name,modified_test)
                save_test_suite(modified_unit_test, self.unit_test_path)
                test_ran, output = self.unit_test_java_executor.run_java()
            save_test_suite(source_code, self.source_code_path)

