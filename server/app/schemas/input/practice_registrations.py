from pydantic import BaseModel, Field

class RegistrationRequest(BaseModel):
    user_id: int = Field(..., gt=0)
    base_id: int = Field(..., gt=0)
