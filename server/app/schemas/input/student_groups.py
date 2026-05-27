from pydantic import BaseModel, Field

class StudentGroupCreate(BaseModel):
    student_id: int = Field(..., gt=0)
    group_id: int = Field(..., gt=0)
