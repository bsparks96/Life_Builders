package models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientEntryRequest {

    @JsonProperty("clientFirstName")
    public String firstName;

    @JsonProperty("clientMiddleInitial")
    public String middleInitial;

    @JsonProperty("clientLastName")
    public String lastName;

    @JsonProperty("clientDOB")
    public String dateOfBirth;

    @JsonProperty("clientSSN")
    public String ssn;

    @JsonProperty("clientGender")
    public String gender;

    @JsonProperty("clientEducation")
    public String educationLevel;

    @JsonProperty("incarcerationPeriods")
    public List<IncarcerationPeriod> incarcerationPeriods;

    @JsonProperty("coursesCompleted")
    public List<CompletedCourse> completedCourses;

    public static class IncarcerationPeriod {
        @JsonProperty("startDate")
        public String startDate;

        @JsonProperty("endDate")
        public String endDate;
    }

    public static class CompletedCourse {
        @JsonProperty("courseID")
        public int courseID;

        @JsonProperty("completionDate")
        public String completionDate;
    }
}
