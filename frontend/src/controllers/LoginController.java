package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import services.AuthService;

import java.io.IOException;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

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

                if (utils.SessionManager.mustChangePassword()) {
                    showForcedPasswordChangePopup(username, password);
                } else {
                    HeaderController.pushScene("Home.fxml");
                }
            });

        }).start();
    }

    @FXML
    private void handleReset() {
        usernameField.clear();
        passwordField.clear();
    }
    
    private void showForcedPasswordChangePopup(String username, String currentPassword) {

        Stage stage = new Stage();
        stage.setTitle("Change Password");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("You must change your password");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button submitButton = new Button("Submit");

        submitButton.setOnAction(e -> {

            String newPass = newPasswordField.getText();
            String confirmPass = confirmPasswordField.getText();

            if (newPass == null || newPass.isBlank()) {
                errorLabel.setText("Password cannot be empty");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                errorLabel.setText("Passwords do not match");
                return;
            }

            boolean success = services.AuthService.changePassword(currentPassword, newPass);

            if (!success) {
                errorLabel.setText("Failed to change password");
                return;
            }

            utils.SessionManager.setMustChangePassword(false);

            stage.close();

            HeaderController.pushScene("Home.fxml");
        });

        stage.setOnCloseRequest(e -> {
            utils.SessionManager.clearSession();
            System.out.println("User forced to logout (password not changed)");
        });

        root.getChildren().addAll(
                title,
                newPasswordField,
                confirmPasswordField,
                submitButton,
                errorLabel
        );

        Scene scene = new Scene(root, 350, 250);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
