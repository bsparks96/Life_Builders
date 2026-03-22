package models;

import java.util.List;

public class CourseDetailsResponse {
    public int courseID;
    public String courseName;
    public List<InstructorOut> instructors;
    public List<IterationOut> iterations;
    
    public List<InstructorOut> getInstructors() {
        return instructors;
    }

    public void setInstructors(List<InstructorOut> instructors) {
        this.instructors = instructors;
    }

    public List<IterationOut> getIterations() {
        return iterations;
    }

    public void setIterations(List<IterationOut> iterations) {
        this.iterations = iterations;
    }
}
/*
class InstructorOut {
    public String instructorName;  // or use userID if you're still working on names
}

class IterationOut {
    public String startDate;
    public String endDate;
}*/
