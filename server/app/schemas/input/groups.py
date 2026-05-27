from pydantic import BaseModel, Field
from typing import Optional

class GroupCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=50)
    year_id: int = Field(..., gt=0)
    course_id: int = Field(..., gt=0)

class GroupUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=50)
    year_id: Optional[int] = Field(None, gt=0)
    course_id: Optional[int] = Field(None, gt=0)
