from pydantic import BaseModel, Field
from typing import Optional

class MessageCreate(BaseModel):
    chat_id: int = Field(..., gt=0)
    sender_id: int = Field(..., gt=0)
    content: str = Field(..., min_length=1)

class MessageUpdate(BaseModel):
    content: Optional[str] = Field(None, min_length=1)
