from pydantic import BaseModel
from typing import List

class MonthlyCompletion(BaseModel):
    month: str
    count: int

class StatisticsResponse(BaseModel):
    activeMonthlyParticipants: int
    completionRate: float
    monthlyCompletions: List[MonthlyCompletion]