from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from models.models import Client, ClientIncarcerationPeriods, CourseHasClients
from utils.database import get_db
from classes.client import ClientSummary, ClientCreate

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
