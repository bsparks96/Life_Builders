package controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import models.ClientEntryRequest;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import java.io.IOException;


public class ClientFormController {

    @FXML private TextField firstNameField;
    @FXML private TextField middleField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker dobPicker;
    @FXML private TextField ssnField;
    @FXML private TextField genderField;
    @FXML private ComboBox<String> educationCombo;
    @FXML private Button submitButton;
    @FXML private Button preAssessmentButton;
    

    @FXML private ComboBox<String> courseCombo1;

    private static Map<String, Integer> courseNameToIdMap = new HashMap<>();
    private static ObservableList<String> courseNames = FXCollections.observableArrayList();

    @FXML private VBox incarcerationBox;
    @FXML private VBox coursesBox;
    

    @FXML private void initialize() {
        educationCombo.getItems().addAll(
            "Did Not Graduate",
            "GED",
            "High School Graduate",
            "Some College",
            "College Degree"
        );
        
        courseCombo1.setItems(utils.CourseCache.getCourseNames());
        
        courseCombo1.setEditable(true);
        courseCombo1.setVisibleRowCount(5);
        
        courseCombo1.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                courseCombo1.setItems(utils.CourseCache.getCourseNames());
            } else {
                ObservableList<String> filtered = FXCollections.observableArrayList();
                for (String name : utils.CourseCache.getCourseNames()) {
                    if (name.toLowerCase().contains(newText.toLowerCase())) {
                        filtered.add(name);
                    }
                }
                courseCombo1.setItems(filtered);
                courseCombo1.show(); 
            }
        });
        
        boolean canSubmit = utils.PermissionUtil.canSubmitClientEntry();

        utils.UIUtil.setEnabled(submitButton, canSubmit);
        utils.UIUtil.setEnabled(preAssessmentButton, canSubmit);
        
        boolean canEdit = utils.PermissionUtil.canSubmitClientEntry();

        setFormEditable(canEdit);
    }

    @FXML
    private void handleViewClients(javafx.event.ActionEvent event) {
    	HeaderController.pushScene("ClientView.fxml");
    }

    @FXML
    private void handlePreAssessment() {
        
    	if (!utils.PermissionUtil.canSubmitClientEntry()) {
            System.out.println("Unauthorized pre-assessment attempt");
            return;
        }
    	
    	// TODO: Transition to pre-assessment view
    }

    @FXML
    private void handleSubmit() {
    	
    	if (!utils.PermissionUtil.canSubmitClientEntry()) {
            System.out.println("Unauthorized submit attempt");
            return;
        }
    	
    	
    	
    	List<String> missingFields = new ArrayList<>();

    	if (firstNameField.getText() == null || firstNameField.getText().isBlank()) {
    	    missingFields.add("First Name");
    	}

    	if (lastNameField.getText() == null || lastNameField.getText().isBlank()) {
    	    missingFields.add("Last Name");
    	}

    	if (dobPicker.getValue() == null) {
    	    missingFields.add("Date of Birth");
    	}

    	if (genderField.getText() == null || genderField.getText().isBlank()) {
    	    missingFields.add("Gender");
    	}

    	if (educationCombo.getValue() == null || educationCombo.getValue().isBlank()) {
    	    missingFields.add("Education Level");
    	}
    	
    	if (!missingFields.isEmpty()) {

    	    StringBuilder message = new StringBuilder("Please fill in the following required fields:\n\n");

    	    for (String field : missingFields) {
    	        message.append("- ").append(field).append("\n");
    	    }

    	    Alert alert = new Alert(Alert.AlertType.WARNING);
    	    alert.setTitle("Missing Required Fields");
    	    alert.setContentText(message.toString());
    	    alert.showAndWait();

    	    return; 
    	}
    	
    	ClientEntryRequest request = new ClientEntryRequest();

        request.firstName = firstNameField.getText();
        request.middleInitial = middleField.getText();
        request.lastName = lastNameField.getText();
        request.dateOfBirth = dobPicker.getValue() != null ? dobPicker.getValue().toString() : null;
        request.ssn = ssnField.getText();
        request.gender = genderField.getText();
        request.educationLevel = educationCombo.getValue();

        request.incarcerationPeriods = new ArrayList<>();
        for (Node node : incarcerationBox.getChildren()) {
            if (node instanceof HBox row) {
                List<Node> inputs = row.getChildren();
                if (inputs.size() >= 2 && inputs.get(0) instanceof DatePicker start && inputs.get(1) instanceof DatePicker end) {
                    if (start.getValue() != null && end.getValue() != null) {
                        ClientEntryRequest.IncarcerationPeriod ip = new ClientEntryRequest.IncarcerationPeriod();
                        ip.startDate = start.getValue().toString();
                        ip.endDate = end.getValue().toString();
                        request.incarcerationPeriods.add(ip);
                    }
                }
            }
        }

        request.completedCourses = new ArrayList<>();
        for (Node node : coursesBox.getChildren()) {
            if (node instanceof HBox row) {
                List<Node> inputs = row.getChildren();
                if (inputs.size() >= 2 && inputs.get(0) instanceof ComboBox<?> courseCombo && inputs.get(1) instanceof DatePicker datePicker) {
                    String selected = (String) courseCombo.getValue();
                    if (utils.CourseCache.getCourseNameToIdMap().containsKey(selected) && datePicker.getValue() != null) {
                        ClientEntryRequest.CompletedCourse course = new ClientEntryRequest.CompletedCourse();
                        course.courseID = utils.CourseCache.getCourseNameToIdMap().get(selected);
                        course.completionDate = datePicker.getValue().toString();
                        request.completedCourses.add(course);
                    }
                }
            }
        }

        boolean success = services.ClientService.submitClientEntry(request);
        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Client successfully submitted!");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to submit client.");
            alert.showAndWait();
        }
    }
    
    

    @FXML
    private void addIncarcerationPeriod() {
        HBox newRow = new HBox(10);

        DatePicker start = new DatePicker();
        DatePicker end = new DatePicker();
        Button addButton = new Button("+");
        addButton.setOnAction(e -> addIncarcerationPeriod()); 

        newRow.getChildren().addAll(start, end, addButton);
        incarcerationBox.getChildren().add(newRow);
    }
    
    @FXML
    private void addCourse() {
        HBox newRow = new HBox(10);

        ComboBox<String> dynamicCourseCombo = new ComboBox<>();
        dynamicCourseCombo.setEditable(true);
        dynamicCourseCombo.setPromptText("Course Name");
        dynamicCourseCombo.setItems(utils.CourseCache.getCourseNames());
        dynamicCourseCombo.setVisibleRowCount(5);

        dynamicCourseCombo.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                dynamicCourseCombo.setItems(utils.CourseCache.getCourseNames());
            } else {
                ObservableList<String> filtered = FXCollections.observableArrayList();
                for (String name : utils.CourseCache.getCourseNames()) {
                    if (name.toLowerCase().contains(newText.toLowerCase())) {
                        filtered.add(name);
                    }
                }
                dynamicCourseCombo.setItems(filtered);
                dynamicCourseCombo.show(); 
            }
        });

        DatePicker courseDate = new DatePicker();

        Button addButton = new Button("+");
        addButton.setOnAction(e -> addCourse());

        newRow.getChildren().addAll(dynamicCourseCombo, courseDate, addButton);
        coursesBox.getChildren().add(newRow);
    }
    
    private void setFormEditable(boolean editable) {

        firstNameField.setEditable(editable);
        middleField.setEditable(editable);
        lastNameField.setEditable(editable);
        dobPicker.setDisable(!editable);
        ssnField.setEditable(editable);
        genderField.setEditable(editable);
        educationCombo.setDisable(!editable);
        courseCombo1.setDisable(!editable);
    }
}
