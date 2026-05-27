from pydantic import BaseModel, Field
from typing import List, Optional

class ChatCreate(BaseModel):
    name: str = Field(..., min_length=1)
    user_ids: List[int] = Field(..., min_length=2)

class ChatUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1)
