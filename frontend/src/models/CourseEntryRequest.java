package models;

import java.util.List;

public class CourseEntryRequest {
    public String courseName;
    public int courseLength;
    public List<Integer> instructorIDs;
    public List<IterationIn> iterations;

    public static class IterationIn {
        public String courseStartDate;
        public String courseEndDate;
    }
}
