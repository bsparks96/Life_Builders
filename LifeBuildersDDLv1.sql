DROP SCHEMA IF EXISTS `LifeBuilders` ;
CREATE DATABASE LifeBuilders;
USE LifeBuilders;

-- DDL Script for Life Builders Database

-- ========================
-- TABLE: Users
-- ========================
CREATE TABLE Users (
    userID INT PRIMARY KEY auto_increment,  -- Starts at 11111
    username VARCHAR(100) NOT NULL UNIQUE,
    firstName VARCHAR(100),
    lastName VARCHAR(100),
    userRole ENUM('Admin', 'Staff', 'Guest') DEFAULT 'Staff'
) AUTO_INCREMENT = 10000;

-- ========================
-- TABLE: Clients
-- ========================
CREATE TABLE Clients (
    clientID INT PRIMARY KEY auto_increment,  -- Starts at 20000
    clientFirstName VARCHAR(100),
    clientMiddleInitial VARCHAR(1),
    clientLastName VARCHAR(100),
    clientEmail VARCHAR(255) UNIQUE,
    clientDOB DATE,
    clientSSN CHAR(9),  -- Consider encryption for real deployment
    clientGender VARCHAR(50),
    clientEducation VARCHAR(21)
) AUTO_INCREMENT = 20000;

CREATE TABLE ClientIncarcerationPeriods (
	incarcerationPeriodID INT PRIMARY KEY auto_increment,
    clientID INT,
    incarcerationStartDate DATE,
    incarcerationEndDate DATE,
    FOREIGN KEY (clientID) REFERENCES Clients(clientID)
);    

-- ========================
-- TABLE: ClientHasPreAssessments
-- ========================
CREATE TABLE clientHasPreAssessments (
    preAssessmentID INT PRIMARY KEY AUTO_INCREMENT,
    clientID INT,
    assessmentCompletionDate DATE,
    FOREIGN KEY (clientID) REFERENCES Clients(clientID)
);

-- ========================
-- TABLE: ClientHasPostAssessments
-- ========================
CREATE TABLE ClientHasPostAssessments (
    postAssessmentID INT PRIMARY KEY AUTO_INCREMENT,
    clientID INT,
    assessmentCompletionDate DATE,
    FOREIGN KEY (clientID) REFERENCES Clients(clientID)
);

-- ========================
-- TABLE: Courses
-- ========================
CREATE TABLE Courses (
    courseID INT PRIMARY KEY auto_increment,
    courseName VARCHAR(200),
    courseDescription TEXT NULL,
    courseLength INT  -- in days or hours
);

CREATE TABLE CoursesHasInstructors (
	courseID INT,
    userID INT,
    PRIMARY KEY (courseID, userID),
    FOREIGN KEY (userID) REFERENCES Users(userID),
    FOREIGN KEY (courseID) REFERENCES Courses(courseID)
);

-- ========================
-- TABLE: CourseIterations
-- ========================
CREATE TABLE CourseIterations (
    iterationID INT PRIMARY KEY auto_increment,
    courseID INT,
    courseStartDate DATE,
    courseEndDate DATE,
    courseLocation VARCHAR(255) NULL,
    FOREIGN KEY (courseID) REFERENCES Courses(courseID)
);



CREATE TABLE CourseHasClients (
    clientID INT NOT NULL,
    courseID INT NOT NULL,
    iterationID INT,
    startDate DATE,
    endDate DATE,
    completionDate DATE,  -- nullable is okay

    PRIMARY KEY (clientID, courseID),

    FOREIGN KEY (clientID) REFERENCES Clients(clientID),
    FOREIGN KEY (courseID) REFERENCES Courses(courseID),
    FOREIGN KEY (iterationID) REFERENCES CourseIterations(iterationID)
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
