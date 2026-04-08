from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from models.models import Course, CourseHasInstructors, CourseIterations, User
from utils.database import get_db
from classes.course import CourseSummary, CourseDetailsResponse, InstructorOut, IterationOut, CourseCreateRequest

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

    iteration_list = [
        IterationOut(
            startDate=it.courseStartDate,
            endDate=it.courseEndDate
        )
        for it in iterations
    ]

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
            iteration = CourseIterations(
                courseID=new_course.courseID,
                courseStartDate=iteration.courseStartDate,
                courseEndDate=iteration.courseEndDate,
                courseLocation=iteration.courseLocation
            )
            db.add(iteration)

        db.commit()
        return {"message": "Course created successfully", "courseID": new_course.courseID}

    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))