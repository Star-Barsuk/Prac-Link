from pydantic import BaseModel, Field
from datetime import date
from typing import Optional

class YearCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=50)
    start_date: date
    end_date: date

class YearUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=50)
    start_date: Optional[date] = None
    end_date: Optional[date] = None
