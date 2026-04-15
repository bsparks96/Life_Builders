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

        if (username == null || username.isBlank() ||
            password == null || password.isBlank()) {

            showAlert("Login Error", "Username and password cannot be empty.");
            return;
        }

        usernameField.setDisable(true);
        passwordField.setDisable(true);

        new Thread(() -> {

            boolean loginSuccess = false;

            try {
                loginSuccess = AuthService.login(username, password);

                if (!loginSuccess) {
                    System.out.println("Login failed, retrying...");

                    Thread.sleep(1500); 

                    loginSuccess = AuthService.login(username, password);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean finalLoginSuccess = loginSuccess;

            javafx.application.Platform.runLater(() -> {

                usernameField.setDisable(false);
                passwordField.setDisable(false);

                if (!finalLoginSuccess) {
                    showAlert("Login Failed", "Unable to connect or invalid credentials.");
                    return;
                }

                boolean userLoaded = AuthService.fetchCurrentUser();

                if (!userLoaded) {
                    showAlert("Session Error", "Failed to retrieve user session.");
                    return;
                }

                HeaderController.pushScene("Home.fxml");
            });

        }).start();
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
