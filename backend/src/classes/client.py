from pydantic import BaseModel, Field
from typing import Optional, List, Tuple
from datetime import date


class IncarcerationPeriod(BaseModel):
    startDate: date
    endDate: date

class CourseCompleted(BaseModel):
    courseID: int
    completionDate: date


class ClientSummary(BaseModel):
    clientID: int
    clientFirstName: str
    clientMiddleInitial: Optional[str] = None
    clientLastName: str


    class Config:
        from_attributes = True


class ClientCreate(BaseModel):
    clientFirstName: str
    clientMiddleInitial: Optional[str] = None
    clientLastName: str
    clientDOB: date
    clientSSN: Optional[str] = None
    clientGender: str
    clientEducation: str
    incarcerationPeriods: Optional[List[IncarcerationPeriod]] = []
    coursesCompleted: Optional[List[CourseCompleted]] = []

class ClientDetailsResponse(BaseModel):
    clientID: int
    fullName: str
    dateOfBirth: date
    gender: str
    education: str

    currentCourse: Optional[dict] = None
    completedCourses: List[dict] = []
    incarcerationPeriods: List[dict] = []

    class Config:
        from_attributes = True

class ClientCourseEnroll(BaseModel):
    clientID: int
    courseID: int
    iterationID: int

class AttendanceUpdate(BaseModel):
    sessionID: int
    clientID: int
    attendance: bool

class AttendanceBulkUpdate(BaseModel):
    updates: List[AttendanceUpdate]

class SessionInfo(BaseModel):
    sessionID: int
    date: date


class ClientAttendance(BaseModel):
    clientID: int
    name: str
    attendance: List[dict]


class IterationAttendanceResponse(BaseModel):
    iterationID: int
    sessions: List[SessionInfo]
    clients: List[ClientAttendance]


class CompletionUpdate(BaseModel):
    clientID: int
    courseID: int
    iterationID: int
    completionDate: date


class CompletionBulkUpdate(BaseModel):
    updates: List[CompletionUpdate]