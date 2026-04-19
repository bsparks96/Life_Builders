from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from models.models import Client, ClientIncarcerationPeriods, CourseHasClients, Course, CourseSessions, SessionAttendance, CourseIterations
from utils.database import get_db
from classes.client import ClientSummary, ClientCreate, ClientDetailsResponse, ClientCourseEnroll, AttendanceBulkUpdate, AttendanceUpdate, IterationAttendanceResponse, CompletionBulkUpdate

router = APIRouter(prefix="/clients", tags=["Clients"])

@router.get("/", response_model=list[ClientSummary])
def get_all_clients(db: Session = Depends(get_db)):
    return db.query(Client).all()

@router.post("/clientEntry/")
def create_client(client: ClientCreate, db: Session = Depends(get_db)):
    try:
        # 1. Create client object
        db_client = Client(
            clientFirstName=client.clientFirstName,
            clientMiddleInitial=client.clientMiddleInitial,
            clientLastName=client.clientLastName,
            clientDOB=client.clientDOB,
            clientSSN=client.clientSSN,
            clientGender=client.clientGender,
            clientEducation=client.clientEducation
        )
        db.add(db_client)
        db.flush()

        # 2. Add incarceration periods
        for period in client.incarcerationPeriods:
            db.add(ClientIncarcerationPeriods(
                clientID=db_client.clientID,
                incarcerationStartDate=period.startDate,
                incarcerationEndDate=period.endDate
            ))

        # 3. Add completed courses
        for course in client.coursesCompleted:
            db.add(CourseHasClients(
                clientID=db_client.clientID,
                courseID=course.courseID,
                completionDate=course.completionDate
            ))

        db.commit()

    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Error creating client: {str(e)}")

    return {"message": "Client created successfully", "clientID": db_client.clientID}


def build_client_details(clientID: int, db: Session):
    client = db.query(Client).filter(Client.clientID == clientID).first()
    if not client:
        return None

    full_name = f"{client.clientFirstName} {client.clientLastName}"

    incarceration_periods = db.query(ClientIncarcerationPeriods).filter(
        ClientIncarcerationPeriods.clientID == clientID
    ).all()

    incarceration_list = [
        {
            "startDate": period.incarcerationStartDate,
            "endDate": period.incarcerationEndDate
        }
        for period in incarceration_periods
    ]

    course_links = db.query(CourseHasClients).filter(
        CourseHasClients.clientID == clientID
    ).all()

    completed_courses = []
    current_course = None

    for link in course_links:
        course = db.query(Course).filter(Course.courseID == link.courseID).first()
        if not course:
            continue

        if link.completionDate:
            completed_courses.append({
                "courseName": course.courseName,
                "completionDate": link.completionDate
            })
        else:
            iteration = db.query(CourseIterations).filter(
                CourseIterations.iterationID == link.iterationID
            ).first()

            current_course = {
                "courseName": course.courseName,
                "startDate": iteration.courseStartDate if iteration else None,
                "endDate": iteration.courseEndDate if iteration else None
            }

    return ClientDetailsResponse(
        clientID=client.clientID,
        fullName=full_name,
        dateOfBirth=client.clientDOB,
        gender=client.clientGender,
        education=client.clientEducation,
        currentCourse=current_course,
        completedCourses=completed_courses,
        incarcerationPeriods=incarceration_list
    )

@router.get("/details/{clientID}", response_model=ClientDetailsResponse)
def get_client_details(clientID: int, db: Session = Depends(get_db)):
    result = build_client_details(clientID, db)

    if not result:
        raise HTTPException(status_code=404, detail="Client not found")

    return result

@router.get("/details/", response_model=list[ClientDetailsResponse])
def get_all_clients_details(db: Session = Depends(get_db)):
    clients = db.query(Client).all()

    results = []

    for client in clients:
        details = build_client_details(client.clientID, db)
        if details:
            results.append(details)

    return results

@router.post("/enroll/")
def enroll_client_in_course(
    request: ClientCourseEnroll,
    db: Session = Depends(get_db)
):
    try:
        # 1. Check if CourseHasClients record exists
        enrollment = db.query(CourseHasClients).filter(
            CourseHasClients.clientID == request.clientID,
            CourseHasClients.iterationID == request.iterationID
        ).first()

        # 2. Create if NOT exists
        if not enrollment:
            enrollment = CourseHasClients(
                clientID=request.clientID,
                courseID=request.courseID,
                iterationID=request.iterationID
            )
            db.add(enrollment)

        # 3. Get all sessions for this iteration
        sessions = db.query(CourseSessions).filter(
            CourseSessions.iterationID == request.iterationID
        ).all()

        created_count = 0

        # 4. Create attendance records if missing
        for session in sessions:

            existing_attendance = db.query(SessionAttendance).filter(
                SessionAttendance.sessionID == session.sessionID,
                SessionAttendance.clientID == request.clientID
            ).first()

            if not existing_attendance:
                attendance = SessionAttendance(
                    sessionID=session.sessionID,
                    clientID=request.clientID,
                    attendance=0
                )
                db.add(attendance)
                created_count += 1

        db.commit()

        return {
            "message": "Client enrollment ensured",
            "sessions_created": created_count
        }

    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

@router.put("/attendance/")
def update_attendance(
    request: AttendanceBulkUpdate,
    db: Session = Depends(get_db)
):
    try:
        updated_count = 0

        for record in request.updates:
            attendance = db.query(SessionAttendance).filter(
                SessionAttendance.sessionID == record.sessionID,
                SessionAttendance.clientID == record.clientID
            ).first()

            if not attendance:
                raise HTTPException(
                    status_code=404,
                    detail=f"Attendance record not found for session {record.sessionID} and client {record.clientID}"
                )

            attendance.attendance = record.attendance
            updated_count += 1

        db.commit()

        return {
            "message": "Attendance updated successfully",
            "records_updated": updated_count
        }

    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/attendance/iteration/{iterationID}", response_model=IterationAttendanceResponse)
def get_iteration_attendance(iterationID: int, db: Session = Depends(get_db)):
    # 1. Get sessions
    sessions = db.query(CourseSessions).filter(
        CourseSessions.iterationID == iterationID
    ).all()

    session_list = [
        {
            "sessionID": s.sessionID,
            "date": s.sessionDate
        }
        for s in sessions
    ]

    session_ids = [s.sessionID for s in sessions]

    # 2. Get enrolled clients
    enrollments = db.query(CourseHasClients).filter(
        CourseHasClients.iterationID == iterationID
    ).all()

    client_ids = [e.clientID for e in enrollments]

    clients = db.query(Client).filter(
        Client.clientID.in_(client_ids)
    ).all()

    # 3. Get attendance records
    attendance_records = db.query(SessionAttendance).filter(
        SessionAttendance.sessionID.in_(session_ids),
        SessionAttendance.clientID.in_(client_ids)
    ).all()

    # 4. Build lookup map (FAST access)
    attendance_map = {}
    for record in attendance_records:
        attendance_map[(record.clientID, record.sessionID)] = record.attendance

    # 5. Build client response
    client_list = []

    for client in clients:
        client_attendance = []

        for session in sessions:
            attended = attendance_map.get(
                (client.clientID, session.sessionID),
                False
            )

            client_attendance.append({
                "sessionID": session.sessionID,
                "attended": bool(attended)
            })

        client_list.append({
            "clientID": client.clientID,
            "name": f"{client.clientFirstName} {client.clientLastName}",
            "attendance": client_attendance
        })

    return {
        "iterationID": iterationID,
        "sessions": session_list,
        "clients": client_list
    }


@router.put("/completion/")
def update_completion_dates(
    request: CompletionBulkUpdate,
    db: Session = Depends(get_db)
):
    try:
        updated_count = 0

        for record in request.updates:

            enrollment = db.query(CourseHasClients).filter(
                CourseHasClients.clientID == record.clientID,
                CourseHasClients.iterationID == record.iterationID
            ).first()

            if not enrollment:
                raise HTTPException(
                    status_code=404,
                    detail=f"Enrollment not found for client {record.clientID}, iteration {record.iterationID}"
                )

            # Optional safety check (courseID match)
            if enrollment.courseID != record.courseID:
                raise HTTPException(
                    status_code=400,
                    detail=f"Course mismatch for client {record.clientID}"
                )

            enrollment.completionDate = record.completionDate
            updated_count += 1

        db.commit()

        return {
            "message": "Completion dates updated successfully",
            "records_updated": updated_count
        }

    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))