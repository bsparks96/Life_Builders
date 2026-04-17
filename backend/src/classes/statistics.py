from pydantic import BaseModel
from typing import List
from datetime import date

class MonthlyCompletion(BaseModel):
    month: str
    count: int

class StatisticsResponse(BaseModel):
    activeMonthlyParticipants: int
    completionRate: float
    monthlyCompletions: List[MonthlyCompletion]

class AttendanceStats(BaseModel):
    attended: int
    total: int

class CompletionStats(BaseModel):
    completed: int
    total: int

class DateRangeStatsResponse(BaseModel):
    startDate: date
    endDate: date
    coursesInRange: int
    sessionsInRange: int
    sessionAttendance: AttendanceStats
    courseCompletion: CompletionStats