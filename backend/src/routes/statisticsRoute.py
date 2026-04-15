from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func, extract
from datetime import datetime
from utils.database import get_db
from classes.statistics import StatisticsResponse, MonthlyCompletion
from models.models import SessionAttendance, CourseSessions, CourseHasClients

router = APIRouter()


@router.get("/statistics/", response_model=StatisticsResponse)
def get_statistics(db: Session = Depends(get_db)):

    current_month = datetime.now().month
    current_year = datetime.now().year

    # Active Monthly Participants

    active_monthly = db.query(
        func.count(func.distinct(SessionAttendance.clientID))
    ).join(
        CourseSessions, SessionAttendance.sessionID == CourseSessions.sessionID
    ).filter(
        SessionAttendance.attendance == True,
        extract('month', CourseSessions.sessionDate) == current_month,
        extract('year', CourseSessions.sessionDate) == current_year
    ).scalar() or 0


    # Completion Rate

    total = db.query(func.count()).select_from(CourseHasClients).scalar() or 0

    completed = db.query(func.count()).filter(
        CourseHasClients.completionDate != None,
        extract('year', CourseHasClients.completionDate) == current_year
    ).scalar() or 0

    completion_rate = round((completed * 100.0 / total), 1) if total > 0 else 0.0


    # Monthly Completions (Jan–Dec)

    results = db.query(
        extract('month', CourseHasClients.completionDate).label("month"),
        func.count().label("count")
    ).filter(
        CourseHasClients.completionDate != None
    ).group_by("month").all()

    month_counts = {int(row.month): row.count for row in results if row.month}

    month_names = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
                   "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]

    monthly_data = []

    for i in range(1, 13):
        monthly_data.append(
            MonthlyCompletion(
                month=month_names[i - 1],
                count=month_counts.get(i, 0)
            )
        )


    return StatisticsResponse(
        activeMonthlyParticipants=active_monthly,
        completionRate=completion_rate,
        monthlyCompletions=monthly_data
    )