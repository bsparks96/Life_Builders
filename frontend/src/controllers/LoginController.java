package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import services.AuthService;

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

     // Basic validation
        if (username == null || username.isBlank() ||
            password == null || password.isBlank()) {

            showAlert("Login Error", "Username and password cannot be empty.");
            return;
        }

        // Attempt login
        boolean loginSuccess = AuthService.login(username, password);

        if (!loginSuccess) {
            showAlert("Login Failed", "Invalid username/password or unable to connect.");
            return;
        }

        // Fetch user details
        boolean userLoaded = AuthService.fetchCurrentUser();

        if (!userLoaded) {
            showAlert("Session Error", "Failed to retrieve user session.");
            return;
        }

        // Navigate to Home
        HeaderController.pushScene("Home.fxml");
    }

    @FXML
    private void handleReset() {
        usernameField.clear();
        passwordField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
        
        
        
        
        
        /*
        
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

        */