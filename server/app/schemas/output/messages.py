from pydantic import BaseModel
from typing import List, Optional

class MessageSenderOutput(BaseModel):
    id: int
    username: str
    email: Optional[str] = None

class MessageOutput(BaseModel):
    id: int
    chat_id: int
    content: str
    sent_at: str
    sender: MessageSenderOutput

class MessageCreateResponse(MessageOutput):
    pass

class MessageDeleteResponse(BaseModel):
    message: str

class ChatMessagesOutput(BaseModel):
    chat_id: int
    count: int
    messages: List[MessageOutput]
