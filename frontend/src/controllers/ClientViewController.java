package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.Client;
import models.ClientDetailsResponse;
import services.ClientService;
import utils.ClientDetailsCache;

public class ClientViewController {

    @FXML private ListView<String> clientList;
    @FXML private Button sortAlphaButton;
    @FXML private Button sortRecentButton;

    @FXML private Label fullNameLabel;
    @FXML private Label dobLabel;
    @FXML private Label genderLabel;
    @FXML private Label educationLabel;

    @FXML private Label currentCourseLabel;
    @FXML private Label currentCourseDateLabel;

    @FXML private VBox completedCoursesBox;
    @FXML private VBox incarcerationPeriodsBox;
    
    private Map<String, Integer> clientNameToID = new HashMap<>();

    @FXML
    public void initialize() {
        try {
        	List<Client> clients = services.ClientService.fetchAllClients();
        	for (Client c : clients) {
        	    String fullName = c.getFullName();

        	    clientList.getItems().add(fullName);
        	    clientNameToID.put(fullName, c.getClientID());
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }

        clientList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        	Integer clientID = clientNameToID.get(newVal);
        	if (clientID != null) {
        	    loadClientInfo(clientID);
        	}
        });

        sortAlphaButton.setOnAction(e -> clientList.getItems().sort(String::compareToIgnoreCase));
        sortRecentButton.setOnAction(e -> FXCollections.reverse(clientList.getItems()));
    }


    private void loadClientInfo(int clientID) {
    	ClientDetailsResponse client = ClientDetailsCache.getClient(clientID);
    	
    	if (client == null) {
    	    client = ClientService.getClientDetails(clientID);
    	}

        if (client == null) return;

        fullNameLabel.setText(client.getFullName());
        dobLabel.setText(client.getDateOfBirth());
        genderLabel.setText(client.getGender());
        educationLabel.setText(client.getEducation());

        // Current course
        if (client.getCurrentCourse() != null) {
            currentCourseLabel.setText(client.getCurrentCourse().getCourseName());
            currentCourseDateLabel.setText(
                client.getCurrentCourse().getStartDate() + " - " +
                client.getCurrentCourse().getEndDate()
            );
        } else {
            currentCourseLabel.setText("None");
            currentCourseDateLabel.setText("");
        }

        // Completed courses
        completedCoursesBox.getChildren().clear();
        for (ClientDetailsResponse.CompletedCourse course : client.getCompletedCourses()) {
            completedCoursesBox.getChildren().add(
                new Label(course.getCourseName() + " – " + course.getCompletionDate())
            );
        }

        // Incarceration periods
        incarcerationPeriodsBox.getChildren().clear();
        for (ClientDetailsResponse.IncarcerationPeriod period : client.getIncarcerationPeriods()) {
            incarcerationPeriodsBox.getChildren().add(
                new Label(period.getStartDate() + " – " + period.getEndDate())
            );
        }
    }
}
