package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // TODO: Validate fields (non-empty)
        // TODO: Send POST request to FastAPI backend (/api/login)
        // TODO: Parse response and determine if login was successful
        // TODO: If successful, load Dashboard.fxml
        // TODO: If failed, show error message
        
        try {
            // Load Home.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Home.fxml"));
            Parent root = loader.load();

            // Get the current stage from the button event
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Optional: Capture current width and height
            double width = stage.getWidth();
            double height = stage.getHeight();

            
            HeaderController.pushScene("Home.fxml");
            // Set the new scene
            //Scene scene = new Scene(root, width, height);  // Set to current size
            //stage.setScene(scene);
            //stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReset() {
        usernameField.clear();
        passwordField.clear();
    }
}
