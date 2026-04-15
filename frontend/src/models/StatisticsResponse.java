package models;

import java.util.List;

public class StatisticsResponse {

    public int activeMonthlyParticipants;
    public double completionRate;
    public List<MonthlyCompletion> monthlyCompletions;

    public static class MonthlyCompletion {
        public String month;
        public int count;
    }
}