package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.Node;

public class HomeController {

    
    @FXML
    private void handleClientRecords(ActionEvent event) {
        HeaderController.pushScene("ClientForm.fxml");
    }

    @FXML
    private void handleTrainingCourses(ActionEvent event) {
        HeaderController.pushScene("CourseManagement.fxml");
    }

    @FXML
    private void handleStatistics(ActionEvent event) {
        HeaderController.pushScene("Statistics.fxml");
    }
}