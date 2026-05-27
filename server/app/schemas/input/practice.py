from pydantic import BaseModel, Field
from typing import Optional

class PracticeBaseCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=255)
    description: str = Field(..., min_length=1)
    capacity: int = Field(..., gt=0)
    year_id: int = Field(..., gt=0)
    course_id: Optional[int] = Field(None, gt=0)
    group_id: Optional[int] = Field(None, gt=0)
    supervisor_id: int = Field(..., gt=0)

class PracticeBaseUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=255)
    description: Optional[str] = None
    capacity: Optional[int] = Field(None, gt=0)
    supervisor_id: Optional[int] = Field(None, gt=0)
