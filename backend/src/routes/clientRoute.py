from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from models.models import Client, ClientIncarcerationPeriods, CourseHasClients, Course
from utils.database import get_db
from classes.client import ClientSummary, ClientCreate, ClientDetailsResponse

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
        db.flush()  # allows access to db_client.clientID before commit

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
            current_course = {
                "courseName": course.courseName,
                "startDate": link.startDate,
                "endDate": link.endDate
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

@router.get("/details", response_model=list[ClientDetailsResponse])
def get_all_clients_details(db: Session = Depends(get_db)):
    clients = db.query(Client).all()

    results = []

    for client in clients:
        details = build_client_details(client.clientID, db)
        if details:
            results.append(details)

    return results