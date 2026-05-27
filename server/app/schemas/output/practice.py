from pydantic import BaseModel
from typing import List

class PracticeBaseOutput(BaseModel):
    id: int
    name: str
    description: str
    capacity: int
    supervisor: str
    supervisor_id: int

class ParticipantOutput(BaseModel):
    id: int
    username: str

class PracticeBaseDetailsOutput(BaseModel):
    base: PracticeBaseOutput
    participants: List[ParticipantOutput]
    available_slots: int
