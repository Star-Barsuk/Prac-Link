from pydantic import BaseModel, EmailStr
from datetime import datetime
from typing import List

class UserOutput(BaseModel):
    id: int
    username: str
    email: EmailStr
    roles: List[str]
    created_at: datetime
