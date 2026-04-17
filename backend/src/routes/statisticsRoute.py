from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func, extract
from datetime import datetime, date
from utils.database import get_db
from classes.statistics import StatisticsResponse, MonthlyCompletion, DateRangeStatsResponse, AttendanceStats, CompletionStats
from models.models import SessionAttendance, CourseSessions, CourseHasClients, CourseIterations

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


@router.get("/statistics/range/", response_model=DateRangeStatsResponse)
def get_statistics_by_date_range(
    startDate: date,
    endDate: date,
    db: Session = Depends(get_db)
):
    try:
        # ----------------------------------
        # Validate input
        # ----------------------------------
        if endDate < startDate:
            raise HTTPException(status_code=400, detail="endDate must be after startDate")

        # ----------------------------------
        # 1. Courses (Iterations) in range
        # Overlap logic:
        # start <= endDate AND end >= startDate
        # ----------------------------------
        courses_in_range = db.query(func.count()).select_from(CourseIterations).filter(
            CourseIterations.courseStartDate <= endDate,
            CourseIterations.courseEndDate >= startDate
        ).scalar() or 0

        # ----------------------------------
        # 2. Sessions in range
        # ----------------------------------
        sessions_in_range = db.query(func.count()).select_from(CourseSessions).filter(
            CourseSessions.sessionDate >= startDate,
            CourseSessions.sessionDate <= endDate
        ).scalar() or 0

        # ----------------------------------
        # 3. Session Attendance (X / Y)
        # ----------------------------------
        attendance_query = db.query(SessionAttendance).join(
            CourseSessions,
            SessionAttendance.sessionID == CourseSessions.sessionID
        ).filter(
            CourseSessions.sessionDate >= startDate,
            CourseSessions.sessionDate <= endDate
        )

        total_attendance = attendance_query.count()

        attended_count = attendance_query.filter(
            SessionAttendance.attendance == True
        ).count()

        # ----------------------------------
        # 4. Course Completion (X / Y)
        # Y = enrollments linked to iterations in range
        # X = completionDate within range
        # ----------------------------------
        completion_query = db.query(CourseHasClients).join(
            CourseIterations,
            CourseHasClients.iterationID == CourseIterations.iterationID
        ).filter(
            CourseIterations.courseStartDate <= endDate,
            CourseIterations.courseEndDate >= startDate
        )

        total_enrollments = completion_query.count()

        completed_count = completion_query.filter(
            CourseHasClients.completionDate != None,
            CourseHasClients.completionDate >= startDate,
            CourseHasClients.completionDate <= endDate
        ).count()

        # ----------------------------------
        # Return response
        # ----------------------------------
        return DateRangeStatsResponse(
            startDate=startDate,
            endDate=endDate,
            coursesInRange=courses_in_range,
            sessionsInRange=sessions_in_range,
            sessionAttendance=AttendanceStats(
                attended=attended_count,
                total=total_attendance
            ),
            courseCompletion=CompletionStats(
                completed=completed_count,
                total=total_enrollments
            )
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))