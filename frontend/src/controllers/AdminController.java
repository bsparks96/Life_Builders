package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.UserDetails;
import services.UserService;
import utils.PermissionUtil;
import utils.ThreadPoolManager;
import utils.UIUtil;
import utils.UserCache;
import utils.UserDetailsCache;

public class AdminController {

    @FXML private ListView<String> userListView;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField userIDField;
    @FXML private TextField usernameField;

    @FXML private ComboBox<String> roleComboBox;

    @FXML private Button createUserButton;
    @FXML private Button updateButton;
    @FXML private Button resetPasswordButton;
    
    private models.UserDetails originalUser = null;

    @FXML
    public void initialize() {
    	
    	if (!PermissionUtil.canManageUsers()) {
            System.out.println("Unauthorized access to Admin page");
            
            // Disable everything
            UIUtil.setEnabled(createUserButton, false);
            UIUtil.setEnabled(updateButton, false);
            UIUtil.setEnabled(resetPasswordButton, false);
            return;
        }    	
    	
    	ThreadPoolManager.submit(() -> {

    	    List<Integer> ids = new ArrayList<>(UserCache.getUserNameToIdMap().values());
    	    List<UserDetails> users = UserService.fetchUserDetailsBulk(ids);

    	    if (users != null) {

    	        Map<String, Integer> newMap = new HashMap<>();
    	        for (UserDetails u : users) {
    	            newMap.put(u.getUsername(), u.getUserID());
    	        }

    	        javafx.application.Platform.runLater(() -> {

    	            UserCache.setAvailableUsers(newMap);
    	            UserDetailsCache.setUserDetails(users);

    	            userListView.refresh();
    	        });
    	    }
    	});

        
        roleComboBox.getItems().addAll("Admin", "Staff", "Guest");

        userListView.setItems(utils.UserCache.getUserNames());
        
        userListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String username, boolean empty) {
                super.updateItem(username, empty);

                if (empty || username == null) {
                    setText(null);
                    return;
                }

                Integer id = UserCache.getUserID(username);

                if (UserDetailsCache.isLoaded() && id != null) {
                    UserDetails user = UserDetailsCache.getUserByID(id);

                    if (user != null) {
                        setText(user.getFirstName() + " " + user.getLastName());
                        return;
                    }
                }

                setText(username);
            }
        });

        userListView.getSelectionModel().clearSelection();
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadUserDetails(newVal);
            }
        });
    }

    private void loadUserDetails(String username) {

    	Integer userID = UserCache.getUserID(username);

        if (userID == null) {
            System.out.println("User ID not found");
            return;
        }

        if (!UserDetailsCache.isLoaded()) {
            // showLoadingState(username, userID);
            return;
        }

        UserDetails user = UserDetailsCache.getUserByID(userID);

        if (user == null) {
            System.out.println("User details not found in cache");
            return;
        }
        
        originalUser = user;

        userIDField.setText(String.valueOf(user.getUserID()));
        usernameField.setText(user.getUsername());
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        roleComboBox.setValue(user.getUserRole());
    }


    @FXML
    private void handleCreateUser() {

        if (!PermissionUtil.canManageUsers()) return;

        Stage stage = new Stage();
        stage.setTitle("Create User");

        VBox root = new VBox(10);
        root.setPrefSize(300, 350);
        root.setStyle("-fx-padding: 15;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Admin", "Staff", "Guest");
        roleBox.setPromptText("Select Role");

        Button submitBtn = new Button("Create");
        Label statusLabel = new Label();

        submitBtn.setOnAction(e -> {

            if (usernameField.getText().isEmpty() ||
                passwordField.getText().isEmpty() ||
                roleBox.getValue() == null) {

                statusLabel.setText("Please fill required fields");
                return;
            }

            models.UserCreateRequest req = new models.UserCreateRequest();
            req.setUsername(usernameField.getText());
            req.setPassword(passwordField.getText());
            req.setFirstName(firstNameField.getText());
            req.setLastName(lastNameField.getText());
            req.setUserRole(roleBox.getValue());

            Integer newUserID = UserService.createUser(req);

            if (newUserID != null) {
                statusLabel.setText("User created!");

                UserCache.getUserNameToIdMap().put(req.getUsername(), newUserID);
                UserCache.getUserNames().add(req.getUsername());

                UserDetails newUser = new UserDetails();
                newUser.setUserID(newUserID);
                newUser.setUsername(req.getUsername());
                newUser.setFirstName(req.getFirstName());
                newUser.setLastName(req.getLastName());
                newUser.setUserRole(req.getUserRole());

                UserDetailsCache.addUser(newUser); 

                userListView.refresh();


            } else {
                statusLabel.setText("Failed to create user");
            }
        });

        root.getChildren().addAll(
                new Label("Create New User"),
                usernameField,
                passwordField,
                firstNameField,
                lastNameField,
                roleBox,
                submitBtn,
                statusLabel
        );
        
        

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleUpdateUser() {

        if (!PermissionUtil.canManageUsers()) return;

        if (originalUser == null) {
            System.out.println("No user selected");
            return;
        }

        models.UserUpdateRequest req = new models.UserUpdateRequest();
        req.setUserID(originalUser.getUserID());

        boolean hasChanges = false;


        if (!safeEquals(originalUser.getUsername(), usernameField.getText())) {
            req.setUsername(usernameField.getText());
            hasChanges = true;
        }

        if (!safeEquals(originalUser.getFirstName(), firstNameField.getText())) {
            req.setFirstName(firstNameField.getText());
            hasChanges = true;
        }

        if (!safeEquals(originalUser.getLastName(), lastNameField.getText())) {
            req.setLastName(lastNameField.getText());
            hasChanges = true;
        }

        if (!safeEquals(originalUser.getUserRole(), roleComboBox.getValue())) {
            req.setUserRole(roleComboBox.getValue());
            hasChanges = true;
        }

        if (!hasChanges) {
            System.out.println("No changes detected");
            return;
        }

        String oldUsername = originalUser.getUsername(); 

        boolean success = services.UserService.updateUser(req);

        if (success) {
        	ThreadPoolManager.submit(() -> {

        	    List<Integer> ids = new ArrayList<>(UserCache.getUserNameToIdMap().values());
        	    List<UserDetails> users = UserService.fetchUserDetailsBulk(ids);

        	    if (users != null) {

        	        Map<String, Integer> newMap = new HashMap<>();
        	        for (UserDetails u : users) {
        	            newMap.put(u.getUsername(), u.getUserID());
        	        }

        	        Platform.runLater(() -> {
        	            UserCache.setAvailableUsers(newMap);
        	            UserDetailsCache.setUserDetails(users);
        	            userListView.refresh();
        	        });
        	    }
        	});
            updateOriginalUserFromUI();
        } else {
            System.out.println("Update failed");
        }
     }
    
    private boolean safeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    private void updateOriginalUserFromUI() {

        if (originalUser == null) return;

        originalUser.setUsername(usernameField.getText());
        originalUser.setFirstName(firstNameField.getText());
        originalUser.setLastName(lastNameField.getText());
        originalUser.setUserRole(roleComboBox.getValue());
    }
    
    private void refreshUserCache(models.UserUpdateRequest req, String oldUsername) {

    	// String oldUsername1 = originalUser.getUsername();
    	String newUsername = req.getUsername();
    	Integer userID = req.getUserID();

    	if (newUsername != null && !newUsername.equals(oldUsername)) {

    	    // Update map
    	    UserCache.getUserNameToIdMap().remove(oldUsername);
    	    UserCache.getUserNameToIdMap().put(newUsername, userID);

    	    var namesList = UserCache.getUserNames();

    	    int index = namesList.indexOf(oldUsername);
    	    if (index != -1) {
    	        namesList.set(index, newUsername);  // preserves position, no duplication
    	    } else {
    	    	namesList.remove(oldUsername);
    	        if (!namesList.contains(newUsername)) {
    	            namesList.add(newUsername);
    	        } // fallback safety
    	    }
    	}

        // Update UserDetailsCache
        if (utils.UserDetailsCache.isLoaded()) {

            models.UserDetails user =
                    utils.UserDetailsCache.getUserByID(userID);

            if (user != null) {

                if (req.getUsername() != null)
                    user.setUsername(req.getUsername());

                if (req.getFirstName() != null)
                    user.setFirstName(req.getFirstName());

                if (req.getLastName() != null)
                    user.setLastName(req.getLastName());

                if (req.getUserRole() != null)
                    user.setUserRole(req.getUserRole());
            }
        }
    }

    @FXML
    private void handleResetPassword() {
        if (!PermissionUtil.canManageUsers()) return;
        
        
        if (!PermissionUtil.canManageUsers()) return;

        String username = userListView.getSelectionModel().getSelectedItem();

        if (username == null) {
            System.out.println("No user selected");
            return;
        }

        Integer userID = UserCache.getUserID(username);

        if (userID == null) {
            System.out.println("User ID not found");
            return;
        }

        models.ResetPasswordResponse response =
                services.UserService.resetPassword(userID);

        if (response == null) {
            System.out.println("Reset failed");
            return;
        }

        showTempPasswordPopup(response.getTemporaryPassword());
        
        System.out.println("Reset Password clicked");
    }
    
    private void showTempPasswordPopup(String tempPassword) {

        Stage stage = new Stage();
        stage.setTitle("Temporary Password");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Temporary Password");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField passwordField = new TextField(tempPassword);
        passwordField.setEditable(false);

        Label warning = new Label(
            "⚠ This password will not be shown again.\nPlease copy it now."
        );
        warning.setStyle("-fx-text-fill: red;");

        Button copyButton = new Button("Copy");

        copyButton.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(tempPassword);
            clipboard.setContent(content);
            System.out.println("Copied to clipboard");
        });

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> stage.close());

        root.getChildren().addAll(
                title,
                passwordField,
                copyButton,
                warning,
                closeButton
        );

        Scene scene = new Scene(root, 350, 220);
        stage.setScene(scene);
        stage.show();
    }
}