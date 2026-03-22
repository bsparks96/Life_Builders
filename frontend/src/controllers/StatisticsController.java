package controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

public class StatisticsController {

    @FXML private Label totalClientsLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label completionRateLabel;
    @FXML private Label activeParticipantsLabel;
    @FXML private BarChart<String, Number> courseCompletionChart;
    @FXML private Button clientReportButton;
    @FXML private Button courseReportButton;

    @FXML
    public void initialize() {
        // Set initial stat values (these will eventually come from the backend)
        totalClientsLabel.setText("Total Clients: 120");
        totalCoursesLabel.setText("Total Courses: 35");
        completionRateLabel.setText("Completion Rate: 85%");
        activeParticipantsLabel.setText("Active Participants: 58");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Course Completions");
        series.getData().add(new XYChart.Data<>("Jan", 20));
        series.getData().add(new XYChart.Data<>("Feb", 25));
        series.getData().add(new XYChart.Data<>("Mar", 23));
        series.getData().add(new XYChart.Data<>("Apr", 22));
        series.getData().add(new XYChart.Data<>("May", 24));
        series.getData().add(new XYChart.Data<>("Jun", 21));
        series.getData().add(new XYChart.Data<>("Jul", 26));
        series.getData().add(new XYChart.Data<>("Aug", 23));

        courseCompletionChart.getData().add(series);
    }

    @FXML
    private void handleClientReport(ActionEvent event) {
        System.out.println("Generating client report...");
        // Add scene navigation or report generation here
    }

    @FXML
    private void handleCourseReport(ActionEvent event) {
        System.out.println("Generating course report...");
        // Add scene navigation or report generation here
    }
}