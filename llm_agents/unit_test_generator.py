from llm_agents.llm_agent import LLMAgent
from langchain.schema import SystemMessage, HumanMessage

class UnitTestGenerator(LLMAgent):
    def __init__(self, api_key,model, temperature, system_prompt_path):
        super().__init__(api_key, model, temperature, system_prompt_path)


    def get_unit_test_for_class(self, session_id: str, get_unit_test_prompt: str) -> str:
        # Retrieve long-term memory specific to the session
        long_term_memory = self.get_long_term_memory(session_id)

        # Compose the prompt including the system message, long-term memory, and user input
        system_message = SystemMessage(content=f"{self.system_prompt}\n\nLong-term memory: {long_term_memory}")

        # Get or create the message history for the session
        history = self.get_chat_history(session_id)

        # Add system message and user prompt to the conversation
        messages = [system_message] + history.messages + [HumanMessage(content=get_unit_test_prompt)]
        response = self.chat_model(messages)

        # Update the session chat history and long-term memory
        history.add_user_message(get_unit_test_prompt)
        history.add_assistant_message(response.content)
        self.update_long_term_memory(session_id, get_unit_test_prompt)

        return response.content
