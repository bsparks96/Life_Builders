package models;

import java.util.List;

public class StatisticsRangeResponse {

    public Summary summary;
    public TimeSeries timeSeries;


    public static class Summary {
        public int coursesInRange;
        public int sessionsInRange;
        public Attendance sessionAttendance;
        public Completion courseCompletion;
    }

    public static class Attendance {
        public int attended;
        public int total;
    }

    public static class Completion {
        public int completed;
        public int total;
    }

    public static class TimeSeries {
        public List<DataPoint> monthly;
        public List<DataPoint> weekly;
    }

    public static class DataPoint {
        public String label;
        public int sessions;
        public int attendance;
        public int completions;
    }
}