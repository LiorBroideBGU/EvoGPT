import logging
import os

from dotenv import load_dotenv
from ollama import AsyncClient

from llm_agents.chat_message_history import ChatMessageHistory

# Load environment variables from the .env file (if present)
load_dotenv()


class LLMAgent:
    def __init__(self, api_key: str, model: str, temperature: float):
        self.logger = logging.getLogger(__name__)
        self.chat_store = {}
        self.model = model
        self.temperature = temperature
        self.long_term_memory = {}
        self.chat_model = AsyncClient(host=os.environ["LLM_HOST"], verify=False)
        self.logger.debug(f"Initialized the LLM agent")

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
        self.chat_store[session_id].messages[1] = {"role": "assistant", "content": fixed_response}

    async def invoke(self, messages: list[dict[str, str]], temperature: float | None = None) -> str:
        """
        Invokes the LLM model with the given messages and temperature.
        :param messages: The messages to invoke the model with
        :param temperature: The temperature to use
        :return: The response from the model
        """
        response = await self.chat_model.chat(model=self.model, messages=messages,
                                              options={"temperature": temperature or self.temperature})
        response_dict = response.model_dump()
        return response_dict["message"]["content"]
