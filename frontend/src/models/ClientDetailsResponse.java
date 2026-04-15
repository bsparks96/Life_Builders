package models;

import java.util.List;

public class ClientDetailsResponse {

    private int clientID;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String education;

    private CurrentCourse currentCourse;
    private List<CompletedCourse> completedCourses;
    private List<IncarcerationPeriod> incarcerationPeriods;


    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public CurrentCourse getCurrentCourse() {
        return currentCourse;
    }

    public void setCurrentCourse(CurrentCourse currentCourse) {
        this.currentCourse = currentCourse;
    }

    public List<CompletedCourse> getCompletedCourses() {
        return completedCourses;
    }

    public void setCompletedCourses(List<CompletedCourse> completedCourses) {
        this.completedCourses = completedCourses;
    }

    public List<IncarcerationPeriod> getIncarcerationPeriods() {
        return incarcerationPeriods;
    }

    public void setIncarcerationPeriods(List<IncarcerationPeriod> incarcerationPeriods) {
        this.incarcerationPeriods = incarcerationPeriods;
    }



    public static class CurrentCourse {
        private String courseName;
        private String startDate;
        private String endDate;

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }

    public static class CompletedCourse {
        private String courseName;
        private String completionDate;

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getCompletionDate() {
            return completionDate;
        }

        public void setCompletionDate(String completionDate) {
            this.completionDate = completionDate;
        }
    }

    public static class IncarcerationPeriod {
        private String startDate;
        private String endDate;

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }
}