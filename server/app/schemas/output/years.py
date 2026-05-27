from pydantic import BaseModel
from datetime import date
from typing import List

class YearOutput(BaseModel):
    id: int
    name: str
    start_date: date
    end_date: date

class YearsListOutput(BaseModel):
    years: List[YearOutput]
