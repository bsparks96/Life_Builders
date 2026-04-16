from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from models.models import Course, CourseHasInstructors, CourseIterations, User, CourseSessions, Client, SessionAttendance, CourseHasClients
from utils.database import get_db
from classes.course import CourseSummary, CourseDetailsResponse, InstructorOut, IterationOut, CourseCreateRequest, CreateIterationWithClientsRequest


router = APIRouter()

@router.get("/courses/", response_model=list[CourseSummary])
def get_all_courses(db: Session = Depends(get_db)):
    return db.query(Course).all()


def build_course_details(courseID: int, db: Session):
    # 1. Get base course info
    course = db.query(Course).filter(Course.courseID == courseID).first()
    if not course:
        return None

    # 2. Get instructors
    instructor_links = db.query(CourseHasInstructors).filter(
        CourseHasInstructors.courseID == courseID
    ).all()

    instructors = [
        InstructorOut(userID=inst.userID)
        for inst in instructor_links
    ]

    # 3. Get iterations
    iterations = db.query(CourseIterations).filter(
        CourseIterations.courseID == courseID
    ).all()

    iteration_list = []

    for it in iterations:
        # Get sessions for this iteration
        sessions = db.query(CourseSessions).filter(
            CourseSessions.iterationID == it.iterationID
        ).all()

        session_dates = [s.sessionDate for s in sessions]

        iteration_list.append(
            IterationOut(
                iterationID=it.iterationID,
                startDate=it.courseStartDate,
                endDate=it.courseEndDate,
                sessions=session_dates
            )
        )

    return CourseDetailsResponse(
        courseID=course.courseID,
        courseName=course.courseName,
        instructors=instructors,
        iterations=iteration_list
    )


@router.get("/courseDetails/{courseID}", response_model=CourseDetailsResponse)
def get_course_details(courseID: int, db: Session = Depends(get_db)):
    result = build_course_details(courseID, db)

    if not result:
        raise HTTPException(status_code=404, detail="Course not found")

    return result


@router.get("/courseDetails/", response_model=list[CourseDetailsResponse])
def get_all_course_details(db: Session = Depends(get_db)):
    courses = db.query(Course).all()

    results = []

    for course in courses:
        details = build_course_details(course.courseID, db)
        if details:
            results.append(details)

    return results


@router.post("/courseCreate/")
def create_course(request: CourseCreateRequest, db: Session = Depends(get_db)):
    try:
        # Step 1: Insert into Courses
        new_course = Course(
            courseName=request.courseName,
            courseLength=request.courseLength
        )
        db.add(new_course)
        db.flush() # get courseID

        # Step 2: Link instructors
        for instructor_id in request.instructorIDs:
            user = db.query(User).filter_by(userID=instructor_id).first()
            if not user:
                raise HTTPException(status_code=404, detail=f"User ID {instructor_id} not found")

            instructor_link = CourseHasInstructors(
                courseID=new_course.courseID,
                userID=instructor_id
            )
            db.add(instructor_link)

            # Step 3: Add iterations (optional)
        for iteration in request.iterations:
            new_iteration = CourseIterations(
                courseID=new_course.courseID,
                courseStartDate=iteration.courseStartDate,
                courseEndDate=iteration.courseEndDate,
                courseLocation=iteration.courseLocation
            )
            db.add(new_iteration)
            db.flush()  # get iterationID

            # 🔥 Create sessions for this iteration
            for session_date in iteration.sessions:
                session = CourseSessions(
                    courseID=new_course.courseID,
                    iterationID=new_iteration.iterationID,
                    sessionDate=session_date
                )
                db.add(session)

        db.commit()
        return {"message": "Course created successfully", "courseID": new_course.courseID}

    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/createIteration/")
def create_iteration_with_clients(
    request: CreateIterationWithClientsRequest,
    db: Session = Depends(get_db)
):
    try:
        course = db.query(Course).filter(Course.courseID == request.courseID).first()
        if not course:
            raise HTTPException(status_code=404, detail="Course not found")


        new_iteration = CourseIterations(
            courseID=request.courseID,
            courseStartDate=request.courseStartDate,
            courseEndDate=request.courseEndDate,
            courseLocation=request.courseLocation
        )
        db.add(new_iteration)
        db.flush()



        sessions = []
        for session_date in request.sessionDates:
            session = CourseSessions(
                courseID=request.courseID,
                iterationID=new_iteration.iterationID,
                sessionDate=session_date
            )
            db.add(session)
            sessions.append(session)

        db.flush()

        enrollment_count = 0
        attendance_count = 0

        if request.clientIDs:

            for client_id in request.clientIDs:

                client = db.query(Client).filter(Client.clientID == client_id).first()
                if not client:
                    raise HTTPException(status_code=404, detail=f"Client {client_id} not found")

                enrollment = db.query(CourseHasClients).filter(
                    CourseHasClients.clientID == client_id,
                    CourseHasClients.iterationID == new_iteration.iterationID
                ).first()

                if not enrollment:
                    enrollment = CourseHasClients(
                        clientID=client_id,
                        courseID=request.courseID,
                        iterationID=new_iteration.iterationID
                    )
                    db.add(enrollment)
                    enrollment_count += 1

                for session in sessions:

                    existing_attendance = db.query(SessionAttendance).filter(
                        SessionAttendance.sessionID == session.sessionID,
                        SessionAttendance.clientID == client_id
                    ).first()

                    if not existing_attendance:
                        attendance = SessionAttendance(
                            sessionID=session.sessionID,
                            clientID=client_id,
                            attendance=0
                        )
                        db.add(attendance)
                        attendance_count += 1

        db.commit()

        return {
            "message": "Iteration created and clients enrolled successfully",
            "iterationID": new_iteration.iterationID,
            "sessions_created": len(sessions),
            "clients_enrolled": enrollment_count,
            "attendance_records_created": attendance_count
        }

    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))