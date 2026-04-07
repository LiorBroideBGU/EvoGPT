from langchain_openai import ChatOpenAI
from llm_agents.chat_message_history import ChatMessageHistory
from langchain_core.messages import SystemMessage
from langchain_ollama import ChatOllama
import logging


class LLMAgent:
    def __init__(self, api_key: str, model: str, temperature: float, is_local: bool = False):
        self.logger = logging.getLogger(__name__)
        self.chat_store = {}
        self.long_term_memory = {}
        self.chat_model = ChatOllama(model=model, temperature=temperature, max_retries=20) if is_local else \
            ChatOpenAI(api_key=api_key, model=model, temperature=temperature, max_retries=20)
        
    def get_chat_history(self, session_id: str) -> ChatMessageHistory:
        """
        Retrieve or create chat history for the session
        :param session_id: The chat session id
        :return: ChatMessageHistory: Instance of ChatMessageHistory
        """
        self.logger.debug(f"Getting chat history for session {session_id}")
        if session_id not in self.chat_store:
            self.chat_store[session_id] = ChatMessageHistory()
        return self.chat_store[session_id]

    def update_long_term_memory(self, session_id: str, input: str):
        """
        Stores significant parts of the conversation in long-term memory
        :param session_id: The chat session id
        :param input: The input string (response or prompt)
        """
        self.logger.debug(f"Updating long-term memory for session {session_id} with input {input}")
        chat_history = self.get_chat_history(session_id)
        chat_history.add_user_message(input)

    def get_long_term_memory(self, session_id: str) -> str:
        """
        Format long-term memory as a single string for the prompt
        :param session_id: The chat session id
        :return: The history prompt
        """
        self.logger.debug(f"Getting long-term memory for session {session_id}")
        return ". ".join(self.long_term_memory.get(session_id, []))

    async def edit_history_response(self, session_id: str, fixed_response: str):
        self.chat_store[session_id].messages[1] = SystemMessage(content=fixed_response)
