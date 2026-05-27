from pydantic import BaseModel, Field
from typing import Optional

class CourseCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=50)

class CourseUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=50)
