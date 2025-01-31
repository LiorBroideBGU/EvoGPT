from langchain_openai import ChatOpenAI
from llm_agents.chat_message_history import ChatMessageHistory

class LLMAgent:
    def __init__(self, api_key, model, temperature):
        self.chat_model = ChatOpenAI(api_key=api_key, model=model, temperature=temperature)
        self.chat_store = {}
        self.long_term_memory = {}
        
    def get_chat_history(self, session_id: str) -> ChatMessageHistory:
        """
        Retrieve or create chat history for the session
        :param session_id: The chat session id
        :return: ChatMessageHistory: Instance of ChatMessageHistory
        """
        if session_id not in self.chat_store:
            self.chat_store[session_id] = ChatMessageHistory()
        return self.chat_store[session_id]

    def update_long_term_memory(self, session_id: str, input: str):
        """
        Stores significant parts of the conversation in long-term memory
        :param session_id: The chat session id
        :param input: The input string (response or prompt)
        """
        if session_id not in self.long_term_memory:
            self.long_term_memory[session_id] = []

        # Add input to long-term memory if it's meaningful (e.g., only if input is long enough)
        if len(input) > 20:
            self.long_term_memory[session_id].append(f"User said: {input}")

        # Keep only the last 5 items in long-term memory
        if len(self.long_term_memory[session_id]) > 5:
            self.long_term_memory[session_id] = self.long_term_memory[session_id][-5:]

    def get_long_term_memory(self, session_id: str) -> str:
        """
        Format long-term memory as a single string for the prompt
        :param session_id: The chat session id
        :return: The history prompt
        """
        return ". ".join(self.long_term_memory.get(session_id, []))

    def edit_history_response(self, session_id: str, fixed_response: str):
        self.chat_store[session_id][0] = fixed_response
