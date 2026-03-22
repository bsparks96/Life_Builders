from sqlalchemy import Column, Integer, String, ForeignKey, Date
from sqlalchemy.orm import relationship
from utils.database import Base

class User(Base):
    __tablename__ = "Users"

    userID = Column(Integer, primary_key=True, index=True)
    username = Column(String(100), unique=True, nullable=False)
    firstName = Column(String(100))
    lastName = Column(String(100))
    userRole = Column(String(50))

class Client(Base):
    __tablename__ = "Clients"

    clientID = Column(Integer, primary_key=True, index=True)
    clientFirstName = Column(String(100))
    clientMiddleInitial = Column(String(1))
    clientLastName = Column(String(100))
    clientEmail = Column(String(255), unique=True)
    clientDOB = Column(Date)
    clientSSN = Column(String(9), nullable=True)
    clientGender = Column(String(50))
    clientEducation = Column(String(21))

    incarceration_periods = relationship("ClientIncarcerationPeriods", back_populates="client",
                                         cascade="all, delete-orphan")
    completed_courses = relationship("CourseHasClients", back_populates="client", cascade="all, delete-orphan")

class Course(Base):
    __tablename__ = "Courses"

    courseID = Column(Integer, primary_key=True, autoincrement=True)
    courseName = Column(String(200))
    courseDescription = Column(String, nullable=True)
    courseLength = Column(Integer)

class CourseHasInstructors(Base):
    __tablename__ = "CoursesHasInstructors"
    courseID = Column(Integer, ForeignKey("Courses.courseID"), primary_key=True)
    userID = Column(Integer, ForeignKey("Users.userID"), primary_key=True)

class CourseIterations(Base):
    __tablename__ = "CourseIterations"

    iterationID = Column(Integer, primary_key=True, autoincrement=True)
    courseID = Column(Integer, ForeignKey("Courses.courseID"))
    courseStartDate = Column(Date)
    courseEndDate = Column(Date)
    courseLocation = Column(String)

class ClientIncarcerationPeriods(Base):
    __tablename__ = "ClientIncarcerationPeriods"

    incarcerationPeriodID = Column(Integer, primary_key=True, autoincrement=True)
    clientID = Column(Integer, ForeignKey("Clients.clientID"))
    incarcerationStartDate = Column(Date)
    incarcerationEndDate = Column(Date)

    client = relationship("Client", back_populates="incarceration_periods")

class CourseHasClients(Base):
    __tablename__ = "CourseHasClients"

    clientID = Column(Integer, ForeignKey("Clients.clientID"), primary_key=True)
    courseID = Column(Integer, ForeignKey("Courses.courseID"), primary_key=True)
    iterationID = Column(Integer, ForeignKey("CourseIterations.iterationID"), nullable=True)
    startDate = Column(Date, nullable=True)
    endDate = Column(Date, nullable=True)
    completionDate = Column(Date, nullable=True)  # now explicitly nullable

    client = relationship("Client", back_populates="completed_courses")


