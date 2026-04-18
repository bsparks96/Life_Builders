from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func, extract
from datetime import datetime, date, timedelta
from utils.database import get_db
from classes.statistics import StatisticsResponse, MonthlyCompletion, DateRangeStatsResponse, AttendanceStats, CompletionStats, SummaryStats, TimeBucket, TimeSeries, DateRangeStatsResponse
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
def get_statistics_by_date_range(startDate: date, endDate: date, db: Session = Depends(get_db)):

    if endDate < startDate:
        raise HTTPException(status_code=400, detail="Invalid date range")

    # =====================================================
    # 🔹 SUMMARY (reuse your previous logic)
    # =====================================================

    courses_in_range = db.query(func.count()).select_from(CourseIterations).filter(
        CourseIterations.courseStartDate <= endDate,
        CourseIterations.courseEndDate >= startDate
    ).scalar() or 0

    sessions_in_range = db.query(func.count()).select_from(CourseSessions).filter(
        CourseSessions.sessionDate.between(startDate, endDate)
    ).scalar() or 0

    attendance_query = db.query(SessionAttendance).join(
        CourseSessions,
        SessionAttendance.sessionID == CourseSessions.sessionID
    ).filter(
        CourseSessions.sessionDate.between(startDate, endDate)
    )

    total_attendance = attendance_query.count()
    attended_count = attendance_query.filter(SessionAttendance.attendance == True).count()

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
        CourseHasClients.completionDate.between(startDate, endDate)
    ).count()

    summary = SummaryStats(
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

    # =====================================================
    # 🔹 MONTHLY GROUPING
    # =====================================================

    monthly_sessions = db.query(
        extract('month', CourseSessions.sessionDate).label("month"),
        func.count().label("count")
    ).filter(
        CourseSessions.sessionDate.between(startDate, endDate)
    ).group_by("month").all()

    monthly_attendance = db.query(
        extract('month', CourseSessions.sessionDate).label("month"),
        func.count().label("count")
    ).join(SessionAttendance).filter(
        CourseSessions.sessionDate.between(startDate, endDate),
        SessionAttendance.attendance == True
    ).group_by("month").all()

    monthly_completions = db.query(
        extract('month', CourseHasClients.completionDate).label("month"),
        func.count().label("count")
    ).filter(
        CourseHasClients.completionDate != None,
        CourseHasClients.completionDate.between(startDate, endDate)
    ).group_by("month").all()

    # Convert to lookup maps
    session_map = {int(r.month): r.count for r in monthly_sessions}
    attendance_map = {int(r.month): r.count for r in monthly_attendance}
    completion_map = {int(r.month): r.count for r in monthly_completions}

    month_names = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]



    monthly_data = []

    for m in range(1, 13):
        if m < startDate.month or m > endDate.month:
            continue

        monthly_data.append(TimeBucket(
            label=month_names[m-1],
            sessions=session_map.get(m, 0),
            attendance=attendance_map.get(m, 0),
            completions=completion_map.get(m, 0)
        ))

    # =====================================================
    # 🔹 WEEKLY GROUPING (Python-based)
    # =====================================================

    def format_date(d):
        return d.strftime("%b %#d")

    weekly_data = []

    current_start = startDate
    week_num = 1

    while current_start <= endDate:
        current_end = min(current_start + timedelta(days=6), endDate)

        label = f"{format_date(current_start)} - {format_date(current_end)}"

        sessions = db.query(func.count()).select_from(CourseSessions).filter(
            CourseSessions.sessionDate.between(current_start, current_end)
        ).scalar() or 0

        attendance = db.query(func.count()).select_from(SessionAttendance).join(
            CourseSessions
        ).filter(
            CourseSessions.sessionDate.between(current_start, current_end),
            SessionAttendance.attendance == True
        ).scalar() or 0

        completions = db.query(func.count()).select_from(CourseHasClients).filter(
            CourseHasClients.completionDate != None,
            CourseHasClients.completionDate.between(current_start, current_end)
        ).scalar() or 0

        weekly_data.append(TimeBucket(
            label=label,
            sessions=sessions,
            attendance=attendance,
            completions=completions
        ))

        current_start = current_end + timedelta(days=1)
        week_num += 1

    # =====================================================
    # FINAL RESPONSE
    # =====================================================

    return DateRangeStatsResponse(
        summary=summary,
        timeSeries=TimeSeries(
            monthly=monthly_data,
            weekly=weekly_data
        )
    )
