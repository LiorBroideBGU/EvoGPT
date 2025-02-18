from langchain_openai import ChatOpenAI
from llm_agents.chat_message_history import ChatMessageHistory
from langchain.schema import SystemMessage

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
        chat_history = self.get_chat_history(session_id)
        chat_history.add_user_message(input)

    def get_long_term_memory(self, session_id: str) -> str:
        """
        Format long-term memory as a single string for the prompt
        :param session_id: The chat session id
        :return: The history prompt
        """
        return ". ".join(self.long_term_memory.get(session_id, []))

    def edit_history_response(self, session_id: str, fixed_response: str):
        self.chat_store[session_id].messages[1] = SystemMessage(content=fixed_response)
