package controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

import utils.ClientDetailsCache;
import utils.CourseCache;
import utils.StatisticsCache;

import models.StatisticsResponse;

import java.util.HashMap;
import java.util.Map;

public class StatisticsController {

    @FXML private Label totalClientsLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label completionRateLabel;
    @FXML private Label activeParticipantsLabel;
    @FXML private BarChart<String, Number> courseCompletionChart;
    @FXML private Button clientReportButton;
    @FXML private Button courseReportButton;

    private static final String[] MONTH_ORDER = {
            "Jan","Feb","Mar","Apr","May","Jun",
            "Jul","Aug","Sep","Oct","Nov","Dec"
    };

    @FXML
    public void initialize() {

        // LOAD FROM CACHE

        int totalClients = ClientDetailsCache.isLoaded()
                ? ClientDetailsCache.getAllClients().size()
                : 0;

        int totalCourses = !CourseCache.getCourseNames().isEmpty()
                ? CourseCache.getCourseNames().size()
                : 0;

        totalClientsLabel.setText("Total Clients: " + totalClients);
        totalCoursesLabel.setText("Total Courses: " + totalCourses);

        // LOAD STATISTICS CACHE

        if (!StatisticsCache.isLoaded()) {
            completionRateLabel.setText("Completion Rate: (loading)");
            activeParticipantsLabel.setText("Active Monthly Participants: (loading)");
            return;
        }

        StatisticsResponse stats = StatisticsCache.getStatistics();

        // SET LABELS

        activeParticipantsLabel.setText(
                "Active Monthly Participants: " + stats.activeMonthlyParticipants
        );

        completionRateLabel.setText(
                "Completion Rate: " + String.format("%.1f", stats.completionRate) + "%"
        );

        // BUILD CHART

        Map<String, Integer> monthMap = new HashMap<>();

        for (StatisticsResponse.MonthlyCompletion mc : stats.monthlyCompletions) {
            monthMap.put(mc.month, mc.count);
        }

        courseCompletionChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Course Completions");

        for (String month : MONTH_ORDER) {
            int count = monthMap.getOrDefault(month, 0);
            series.getData().add(new XYChart.Data<>(month, count));
        }

        courseCompletionChart.getData().add(series);
    }

    @FXML
    private void handleClientReport(ActionEvent event) {
        System.out.println("Generating client report...");
    }

    @FXML
    private void handleCourseReport(ActionEvent event) {
        System.out.println("Generating course report...");
    }
}