class ChatMessageHistory:
    """A simple message history manager to track conversation history."""

    def __init__(self):
        self.messages = []

    def add_user_message(self, content: str):
        self.messages.append({"role": "user", "content": content})

    async def add_assistant_message(self, content: str):
        self.messages.append({"role": "assistant", "content": content})

    def clear(self):
        self.messages = []