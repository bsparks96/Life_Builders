package models;

import java.util.List;

public class CreateIterationRequest {

    public int courseID;
    public List<Integer> clientIDs;        
    public String courseStartDate;
    public String courseEndDate;
    public String courseLocation;
    public List<String> sessionDates;      

    public CreateIterationRequest() {}

    public CreateIterationRequest(
            int courseID,
            List<Integer> clientIDs,
            String courseStartDate,
            String courseEndDate,
            String courseLocation,
            List<String> sessionDates
    ) {
        this.courseID = courseID;
        this.clientIDs = clientIDs;
        this.courseStartDate = courseStartDate;
        this.courseEndDate = courseEndDate;
        this.courseLocation = courseLocation;
        this.sessionDates = sessionDates;
    }
}