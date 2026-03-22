DROP SCHEMA IF EXISTS `LifeBuilders` ;
CREATE DATABASE LifeBuilders;
USE LifeBuilders;

-- DDL Script for Life Builders Database

-- ========================
-- TABLE: Users
-- ========================
CREATE TABLE Users (
    userID INT PRIMARY KEY,  -- Starts at 11111
    username VARCHAR(100) NOT NULL UNIQUE,
    firstName VARCHAR(100),
    lastName VARCHAR(100),
    userRole VARCHAR(50)
);

-- ========================
-- TABLE: Students
-- ========================
CREATE TABLE Students (
    studentID INT PRIMARY KEY,  -- Starts at 20000
    studentFirstName VARCHAR(100),
    studentLastName VARCHAR(100),
    studentEmail VARCHAR(255),
    studentDOB DATE,
    studentSSN CHAR(9),  -- Consider encryption for real deployment
    studentGender VARCHAR(50)
);

-- ========================
-- TABLE: StudentHasPreAssessments
-- ========================
CREATE TABLE StudentHasPreAssessments (
    preAssessmentID INT PRIMARY KEY AUTO_INCREMENT,
    studentID INT,
    assessmentCompletionDate DATE,
    FOREIGN KEY (studentID) REFERENCES Students(studentID)
);

-- ========================
-- TABLE: StudentHasPostAssessments
-- ========================
CREATE TABLE StudentHasPostAssessments (
    postAssessmentID INT PRIMARY KEY AUTO_INCREMENT,
    studentID INT,
    assessmentCompletionDate DATE,
    FOREIGN KEY (studentID) REFERENCES Students(studentID)
);

-- ========================
-- TABLE: Courses
-- ========================
CREATE TABLE Courses (
    courseID INT PRIMARY KEY,
    courseName VARCHAR(200),
    courseDescription TEXT,
    courseLength INT  -- in days or hours
);

-- ========================
-- TABLE: CourseIterations
-- ========================
CREATE TABLE CourseIterations (
    iterationID INT PRIMARY KEY,
    courseID INT,
    courseInstructorID INT,
    courseStartDate DATE,
    courseEndDate DATE,
    FOREIGN KEY (courseID) REFERENCES Courses(courseID),
    FOREIGN KEY (courseInstructorID) REFERENCES Users(userID)
);

-- ========================
-- TABLE: CourseHasStudents
-- ========================
CREATE TABLE CourseHasStudents (
    iterationID INT,
    studentID INT,
    completionDate DATE,
    PRIMARY KEY (iterationID, studentID),
    FOREIGN KEY (iterationID) REFERENCES CourseIterations(iterationID),
    FOREIGN KEY (studentID) REFERENCES Students(studentID)
);

-- ========================
-- TABLE: CommunityEvents
-- ========================
CREATE TABLE CommunityEvents (
    commEventID INT PRIMARY KEY,
    eventName VARCHAR(255),
    commStartDate DATE,
    commEndDate DATE,
    commLocation VARCHAR(255),
    commEventSize INT
);

-- ========================
-- TABLE: CommunityEventLeaders
-- ========================
CREATE TABLE CommunityEventLeaders (
    commEventID INT,
    userID INT,
    PRIMARY KEY (commEventID, userID),
    FOREIGN KEY (commEventID) REFERENCES CommunityEvents(commEventID),
    FOREIGN KEY (userID) REFERENCES Users(userID)
);
