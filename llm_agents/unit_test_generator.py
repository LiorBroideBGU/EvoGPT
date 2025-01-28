from llm_agents.llm_agent import LLMAgent
from langchain.schema import SystemMessage, HumanMessage
import os


class UnitTestGenerator(LLMAgent):
    def __init__(self, api_key,model, temperature):
        super().__init__(api_key, model, temperature)
        self.input_prompt = open(os.path.abspath(os.path.join("..", "prompts", "unit_test_generator", "input_prompt.txt")),'r').read()
        self.system_prompt = open(os.path.abspath(os.path.join("..", "prompts", "unit_test_generator", "system_prompt.txt")),'r').read()

    def get_unit_test_for_class(self, session_id: str) -> str:
        # Retrieve long-term memory specific to the session
        long_term_memory = self.get_long_term_memory(session_id)

        # Compose the prompt including the system message, long-term memory, and user input
        system_message =SystemMessage(content=f"{self.system_prompt}\n\nLong-term memory: {long_term_memory}") if self.long_term_memory else SystemMessage(content=f"{self.system_prompt}")

        # Get or create the message history for the session
        history = self.get_chat_history(session_id)

        # Add system message and user prompt to the conversation
        messages = [system_message] + history.messages + [HumanMessage(content=self.input_prompt)]
        response = self.chat_model(messages)

        # Update the session chat history and long-term memory
        history.add_user_message(self.input_prompt)
        history.add_assistant_message(response.content)
        self.update_long_term_memory(session_id, self.input_prompt)

        return response.content

