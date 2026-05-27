from pydantic import BaseModel
from typing import Optional

class RoleOutput(BaseModel):
    id: int
    name: str
    description: Optional[str] = None
