# classes/course.py
from pydantic import BaseModel
from typing import List, Optional
from datetime import date

class CourseSummary(BaseModel):
    courseID: int
    courseName: str

    class Config:
        orm_mode = True

class InstructorOut(BaseModel):
    userID: int

class IterationOut(BaseModel):
    startDate: date
    endDate: date

class CourseDetailsResponse(BaseModel):
    courseID: int
    courseName: str
    instructors: list[InstructorOut]
    iterations: list[IterationOut]

class IterationIn(BaseModel):
    courseStartDate: date
    courseEndDate: date
    courseLocation: Optional[str] = None

class CourseCreateRequest(BaseModel):
    courseName: str
    courseLength: int
    instructorIDs: List[int]  # Assuming front-end sends actual user IDs
    iterations: Optional[List[IterationIn]] = []