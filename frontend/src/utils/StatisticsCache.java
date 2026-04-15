package utils;

import java.util.List;
import models.StatisticsResponse;

public class StatisticsCache {

	private static StatisticsResponse stats = null;

    public static void setStatistics(StatisticsResponse newStats) {
        stats = newStats;
    }

    public static StatisticsResponse getStatistics() {
        return stats;
    }

    public static Integer getActiveMonthlyParticipants() {
        return stats != null ? stats.activeMonthlyParticipants : null;
    }

    public static Double getCompletionRate() {
        return stats != null ? stats.completionRate : null;
    }

    public static java.util.List<StatisticsResponse.MonthlyCompletion> getMonthlyCompletions() {
        return stats != null ? stats.monthlyCompletions : null;
    }

    public static boolean isLoaded() {
        return stats != null;
    }

    public static void clear() {
        stats = null;
    }
}