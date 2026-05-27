from pydantic import BaseModel, Field

class ChatMemberCreate(BaseModel):
    chat_id: int = Field(..., gt=0)
    user_id: int = Field(..., gt=0)
