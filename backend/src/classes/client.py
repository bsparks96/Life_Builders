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