package controllers;

import javafx.fxml.FXML;
import utils.CourseCache;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.CourseDetailsResponse;
import models.CourseEntryRequest;
import services.CourseService;
import models.InstructorOut;
import models.IterationOut;


public class CourseManagementController {

    @FXML private ListView<String> courseListView;
    @FXML private StackPane detailsPane;
    @FXML private Button newCourseButton;
    @FXML private TextField courseNameField;
    @FXML private TextField instructorField;
    @FXML private TextField courseLengthField;
    @FXML private ListView<String> instructorListView;
    @FXML private ComboBox<String> instructorCombo;


    @FXML private VBox courseDetailPane;
    @FXML private VBox iterationsBox;
    @FXML private DatePicker iterationStartPicker;
    @FXML private DatePicker iterationEndPicker;


    @FXML
    public void initialize() {
        // TODO: Fetch courses from backend and populate courseListView
        // courseListView.getItems().addAll(courseService.fetchAllCourses());
    	
    	courseListView.setItems(CourseCache.getCourseNames());
    	
        courseListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showCourseDetails(newVal);
            }
        });
        
        iterationStartPicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (iterationEndPicker.getValue() == null && newDate != null) {
                String weeksStr = courseLengthField.getText();
                try {
                    int weeks = Integer.parseInt(weeksStr);
                    iterationEndPicker.setValue(newDate.plusWeeks(weeks));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid course length format.");
                }
            }
        });
        
        
        instructorListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        instructorListView.getItems().addAll(utils.UserCache.getUserNames());
        instructorCombo.setItems(FXCollections.observableArrayList(utils.UserCache.getUserNames()));

    }

    @FXML
    private void sortAlphabetically() {
        FXCollections.sort(courseListView.getItems(), String::compareToIgnoreCase);
    }

    @FXML
    private void sortMostRecent() {
        // TODO: Sort the list by recent dates
    }

    @FXML
    private void handleNewCourse() {
        detailsPane.getChildren().clear();
        VBox form = new VBox(10);
        form.getChildren().addAll(
            new Label("Course Name:"),
            new TextField(),
            new Label("Instructors:"),
            new TextField(),
            new Button("Save")
        );
        detailsPane.getChildren().add(form);
        instructorListView.getSelectionModel().clearSelection();

    }
    
    @FXML
    private void handleAddIteration(ActionEvent event) {
        HBox iterationRow = new HBox(10);

        DatePicker newStartPicker = new DatePicker();
        newStartPicker.setPromptText("Start Date");
        newStartPicker.setMaxWidth(150);

        DatePicker newEndPicker = new DatePicker();
        newEndPicker.setPromptText("End Date");
        newEndPicker.setMaxWidth(150);

        // Listener: auto-set end date based on course length if empty
        newStartPicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newEndPicker.getValue() == null && newDate != null) {
                try {
                    int weeks = Integer.parseInt(courseLengthField.getText());
                    newEndPicker.setValue(newDate.plusWeeks(weeks));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid course length format.");
                }
            }
        });

        Button removeBtn = new Button("Remove");
        removeBtn.setOnAction(e -> iterationsBox.getChildren().remove(iterationRow));

        iterationRow.getChildren().addAll(newStartPicker, newEndPicker, removeBtn);
        iterationsBox.getChildren().add(iterationRow);
    }
    
    @FXML
    private void addInstructor(ActionEvent event) {
        String selected = instructorCombo.getValue();
        if (selected != null && !selected.isBlank() && !instructorListView.getItems().contains(selected)) {
            instructorListView.getItems().add(selected);
        }
    }

    
    @FXML
    private void handleSaveCourse(ActionEvent event) {
        try {
            CourseEntryRequest req = new CourseEntryRequest();
            req.courseName = courseNameField.getText().trim();

            // Parse course length
            try {
                req.courseLength = Integer.parseInt(courseLengthField.getText().trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Course length must be a number.");
                return;
            }

            // Corrected: Get ALL instructors listed, not just selected
            req.instructorIDs = new ArrayList<>();
            for (String instructorName : instructorListView.getItems()) {
                Integer id = utils.UserCache.getUserID(instructorName);
                if (id != null) {
                    req.instructorIDs.add(id);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Invalid instructor: " + instructorName);
                    return;
                }
            }

            // Gather iterations
            req.iterations = new ArrayList<>();
            for (Node node : iterationsBox.getChildren()) {
                if (node instanceof HBox row) {
                    List<Node> inputs = row.getChildren();
                    if (inputs.size() >= 2 &&
                        inputs.get(0) instanceof DatePicker start &&
                        inputs.get(1) instanceof DatePicker end) {

                        CourseEntryRequest.IterationIn iter = new CourseEntryRequest.IterationIn();
                        iter.courseStartDate = start.getValue() != null ? start.getValue().toString() : null;
                        iter.courseEndDate = end.getValue() != null ? end.getValue().toString() : null;
                        //iter.courseLocation = ""; // optional
                        if (iter.courseStartDate != null && iter.courseEndDate != null) {
                            req.iterations.add(iter);
                        }
                    }
                }
            }

            // Debug Output
            System.out.println("Request Body:");
            System.out.println("Course Name: " + req.courseName);
            System.out.println("Course Length: " + req.courseLength);
            System.out.println("Instructor IDs: " + req.instructorIDs);
            System.out.println("Iterations:");
            for (CourseEntryRequest.IterationIn iter : req.iterations) {
                System.out.println("- Start: " + iter.courseStartDate + ", End: " + iter.courseEndDate);
            }

            // Submit to API
            boolean success = CourseService.submitCourseEntry(req);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Course created successfully!");
                courseDetailPane.setVisible(false);
                courseDetailPane.setManaged(false);
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed to create course.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "An unexpected error occurred.");
        }
    }
    
    
    private void showAlert(Alert.AlertType type, String msg) {
    	Alert alert = new Alert(type, msg);
        alert.showAndWait();
		
	}

	@FXML
    private void handleNewCourseClick(ActionEvent event) {
        // Toggle visibility of form or clear fields as needed
        courseNameField.clear();
        instructorListView.getItems().clear();
        courseLengthField.clear();
        iterationStartPicker.setValue(null);
        iterationEndPicker.setValue(null);
        courseDetailPane.setVisible(true);
        courseDetailPane.setManaged(true);
        instructorCombo.setValue(null);
    }
    
    

    private void showCourseDetails(String courseName) {
    	Integer courseID = CourseCache.getCourseID(courseName);
        if (courseID == null) {
            System.err.println("Course ID not found for course: " + courseName);
            return;
        }

        try {
            CourseDetailsResponse details = CourseService.fetchCourseDetails(courseID);

            VBox detailBox = new VBox(10);
            detailBox.getChildren().add(new Text(courseName + ":"));

            // Instructors
            StringBuilder instructorList = new StringBuilder("Instructors:\n");
            for (InstructorOut instructor : details.getInstructors()) {
            	String instructorName = utils.UserCache.getUserNameByID(instructor.getUserID());
            	instructorList.append("- ").append(instructorName).append("\n");
            }
            detailBox.getChildren().add(new Label(instructorList.toString()));

            // Iterations
            StringBuilder iterationList = new StringBuilder("Current Iterations:\n");
            for (IterationOut iteration : details.getIterations()) {
                iterationList.append("- ").append(iteration.getStartDate()).append(" to ").append(iteration.getEndDate()).append("\n");
            }
            detailBox.getChildren().add(new Label(iterationList.toString()));

            // Placeholder for future clickable past iterations
            detailBox.getChildren().add(new Label("Past Iterations: (clickable items to view clients)"));

            detailsPane.getChildren().clear();
            detailsPane.getChildren().add(detailBox);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching course details for ID: " + courseID);
        }
    }
}