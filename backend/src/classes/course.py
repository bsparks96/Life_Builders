# classes/course.py
from pydantic import BaseModel
from typing import List, Optional
from datetime import date

class CourseSummary(BaseModel):
    courseID: int
    courseName: str

    class Config:
        from_attributes = True

class InstructorOut(BaseModel):
    userID: int

class IterationOut(BaseModel):
    iterationID: int
    startDate: date
    endDate: date
    sessions: List[date] = []

class CourseDetailsResponse(BaseModel):
    courseID: int
    courseName: str
    instructors: list[InstructorOut]
    iterations: list[IterationOut]

class IterationIn(BaseModel):
    courseStartDate: date
    courseEndDate: date
    courseLocation: Optional[str] = None
    sessions: List[date] = []

class CourseCreateRequest(BaseModel):
    courseName: str
    courseLength: int
    instructorIDs: List[int]
    iterations: Optional[List[IterationIn]] = []

from typing import List, Optional
from datetime import date
from pydantic import BaseModel

class CreateIterationWithClientsRequest(BaseModel):
    courseID: int
    clientIDs: Optional[List[int]] = None
    courseStartDate: date
    courseEndDate: date
    courseLocation: Optional[str] = None
    sessionDates: List[date]