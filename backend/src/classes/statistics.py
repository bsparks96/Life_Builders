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

class SummaryStats(BaseModel):
    coursesInRange: int
    sessionsInRange: int
    sessionAttendance: AttendanceStats
    courseCompletion: CompletionStats

class TimeBucket(BaseModel):
    label: str
    sessions: int
    attendance: int
    completions: int

class TimeSeries(BaseModel):
    monthly: List[TimeBucket]
    weekly: List[TimeBucket]

class DateRangeStatsResponse(BaseModel):
    summary: SummaryStats
    timeSeries: TimeSeries