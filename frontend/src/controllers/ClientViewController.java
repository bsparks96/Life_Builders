package controllers;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.Client;

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

    @FXML
    public void initialize() {
        try {
            List<Client> clients = services.ClientService.fetchAllClients();
            for (Client c : clients) {
                clientList.getItems().add(c.getFullName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        clientList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadClientInfo(newVal);
            }
        });

        sortAlphaButton.setOnAction(e -> clientList.getItems().sort(String::compareToIgnoreCase));
        sortRecentButton.setOnAction(e -> FXCollections.reverse(clientList.getItems()));
    }


    private void loadClientInfo(String clientName) {
        // Stub data for now
        fullNameLabel.setText(clientName);
        dobLabel.setText("01/01/1990");
        genderLabel.setText("Male");
        educationLabel.setText("GED");

        currentCourseLabel.setText("Job Skills Training");
        currentCourseDateLabel.setText("12/15/2025");

        completedCoursesBox.getChildren().clear();
        completedCoursesBox.getChildren().addAll(
            new Label("Anger Management – 03/10/2024"),
            new Label("Budgeting Basics – 06/01/2024")
        );

        incarcerationPeriodsBox.getChildren().clear();
        incarcerationPeriodsBox.getChildren().addAll(
            new Label("01/01/2018 – 01/01/2020 (2 yrs)"),
            new Label("03/15/2021 – 10/15/2022 (1 yr, 7 mos)")
        );
    }
}
