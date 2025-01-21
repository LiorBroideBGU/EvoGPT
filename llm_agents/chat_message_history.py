from langchain.schema import SystemMessage, HumanMessage


class ChatMessageHistory:
    """A simple message history manager to track conversation history."""

    def __init__(self):
        self.messages = []

    def add_user_message(self, content: str):
        self.messages.append(HumanMessage(content=content))

    def add_assistant_message(self, content: str):
        self.messages.append(SystemMessage(content=content))

    def clear(self):
        self.messages = []