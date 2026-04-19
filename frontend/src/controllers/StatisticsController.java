package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;




import utils.ClientDetailsCache;
import utils.CourseCache;
import utils.PermissionUtil;
import utils.StatisticsCache;
import utils.UIUtil;
import models.StatisticsResponse;

import java.io.File;
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
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private ToggleButton monthlyToggle;
    @FXML private ToggleButton weeklyToggle;

    @FXML private Button generateReportBtn;

    private static final String[] MONTH_ORDER = {
            "Jan","Feb","Mar","Apr","May","Jun",
            "Jul","Aug","Sep","Oct","Nov","Dec"
    };
    private models.StatisticsRangeResponse lastRangeResponse = null;
    private String lastStartDate = null;
    private String lastEndDate = null;
    private Stage reportStage = null;

    @FXML
    public void initialize() {


        int totalClients = ClientDetailsCache.isLoaded()
                ? ClientDetailsCache.getAllClients().size()
                : 0;

        int totalCourses = !CourseCache.getCourseNames().isEmpty()
                ? CourseCache.getCourseNames().size()
                : 0;

        totalClientsLabel.setText("Total Clients: " + totalClients);
        totalCoursesLabel.setText("Total Courses: " + totalCourses);


        if (!StatisticsCache.isLoaded()) {
            completionRateLabel.setText("Completion Rate: (loading)");
            activeParticipantsLabel.setText("Active Monthly Participants: (loading)");
            return;
        }

        StatisticsResponse stats = StatisticsCache.getStatistics();


        activeParticipantsLabel.setText(
                "Active Monthly Participants: " + stats.activeMonthlyParticipants
        );

        completionRateLabel.setText(
                "Completion Rate: " + String.format("%.1f", stats.completionRate) + "%"
        );


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
        
        ToggleGroup toggleGroup = new ToggleGroup();

        monthlyToggle.setToggleGroup(toggleGroup);
        weeklyToggle.setToggleGroup(toggleGroup);

        monthlyToggle.setSelected(true);
        UIUtil.setEnabled(generateReportBtn, PermissionUtil.canGenerateReports());
    }

    @FXML
    private void handleClientReport(ActionEvent event) {
        System.out.println("Generating client report...");
    }

    @FXML
    private void handleCourseReport(ActionEvent event) {
        System.out.println("Generating course report...");
    }
    
    @FXML
    private void handleGenerateReport() {

        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            System.out.println("Start and End dates are required");
            return;
        }

        String start = startDatePicker.getValue().toString();
        String end = endDatePicker.getValue().toString();
        
        boolean sameRequest =
                start.equals(lastStartDate) &&
                end.equals(lastEndDate) &&
                lastRangeResponse != null;
        
        if (sameRequest) {
            System.out.println("Using cached statistics response");

            if (reportStage != null && reportStage.isShowing()) {
                updateChart();
                return;
            } else {
                openReportPopup(lastRangeResponse);
                return;
            }
        }

        lastRangeResponse =
                services.StatisticsService.fetchRange(start, end);

        if (lastRangeResponse == null) {
            System.out.println("Failed to fetch stats");
            return;
        }
        
        lastStartDate = start;
        lastEndDate = end;

        if (reportStage != null && reportStage.isShowing()) {
            updateChart();
            return;
        } else {
            openReportPopup(lastRangeResponse);
            return;
        }
    }
    
    private void openReportPopup(models.StatisticsRangeResponse data) {

    	if (reportStage == null) {
    	    reportStage = new Stage();
    	}

    	reportStage.setTitle("Report");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

        currentChart = chart; // store reference for toggle updates

        boolean isMonthly = monthlyToggle.isSelected();

        populateChart(chart, data, isMonthly);

        
        Button downloadBtn = new Button("Download PNG");
        
        
        HBox content = new HBox(20);
        
        VBox summaryBox = buildSummary(data);

        HBox.setHgrow(chart, Priority.ALWAYS);
        chart.setMaxWidth(Double.MAX_VALUE);
        chart.setMaxHeight(Double.MAX_VALUE);
        
        content.getChildren().addAll(summaryBox, chart);
        
        HBox buttonRow = new HBox(downloadBtn);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        buttonRow.setPadding(new Insets(10));


        VBox root = new VBox();


        root.getChildren().addAll(content, buttonRow);

        root.setPrefSize(900, 500);
        root.setPadding(new Insets(10));
        
        Scene scene = new Scene(root, 900, 500);
        downloadBtn.setOnAction(e -> downloadAsPNG(chart));
        
        reportStage.setScene(scene);
        reportStage.setResizable(true);
        reportStage.show();
        reportStage.toFront();
    }
    
    private void updateChart() {
        if (lastRangeResponse == null) return;

        boolean isMonthly = monthlyToggle.isSelected();

        populateChart(currentChart, lastRangeResponse, isMonthly);
    }
    
    private BarChart<String, Number> currentChart;

    private void populateChart(
            BarChart<String, Number> chart,
            models.StatisticsRangeResponse data,
            boolean isMonthly
    ) {

        chart.getData().clear();

        var points = isMonthly
                ? data.timeSeries.monthly
                : data.timeSeries.weekly;

        XYChart.Series<String, Number> sessionsSeries = new XYChart.Series<>();
        sessionsSeries.setName("Sessions");

        XYChart.Series<String, Number> attendanceSeries = new XYChart.Series<>();
        attendanceSeries.setName("Attendance");

        for (var point : points) {
            sessionsSeries.getData().add(new XYChart.Data<>(point.label, point.sessions));
            attendanceSeries.getData().add(new XYChart.Data<>(point.label, point.attendance));
        }

        chart.getData().addAll(sessionsSeries, attendanceSeries);
    }
    
    private VBox buildSummary(models.StatisticsRangeResponse data) {

        var summary = data.summary;

        VBox box = new VBox(10);
        
        box.setPadding(new Insets(20, 20, 20, 20));

        Label title = new Label("Summary");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label courses = new Label("Courses: " + summary.coursesInRange);
        Label sessions = new Label("Sessions: " + summary.sessionsInRange);

        Label attendance = new Label(
                "Attendance: " + summary.sessionAttendance.attended +
                " / " + summary.sessionAttendance.total
        );

        Label completion = new Label(
                "Completion: " + summary.courseCompletion.completed +
                " / " + summary.courseCompletion.total
        );

        box.getChildren().addAll(title, courses, sessions, attendance, completion);

        return box;
    }
    
    private void downloadAsPNG(Node node) {

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report");

            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG Image", "*.png")
            );
            
            String start = startDatePicker.getValue().toString();
            String end = endDatePicker.getValue().toString();
            String mode = monthlyToggle.isSelected() ? "Monthly" : "Weekly";

            String defaultName = "Report_" + start + "_to_" + end + "_" + mode + ".png";
            
            fileChooser.setInitialFileName(defaultName);

            File file = fileChooser.showSaveDialog(reportStage);

            if (file == null) return;

            SnapshotParameters params = new SnapshotParameters();
            params.setTransform(javafx.scene.transform.Transform.scale(2, 2));
            
            WritableImage image = node.snapshot(params, null);

            ImageIO.write(
                    SwingFXUtils.fromFXImage(image, null),
                    "png",
                    file
            );

            System.out.println("Saved image: " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}