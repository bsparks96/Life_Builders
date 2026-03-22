package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Stack;

public class HeaderController {

    private static final Stack<String> historyStack = new Stack<>();
    private static final Stack<String> forwardStack = new Stack<>();
    private static Stage mainStage;
    
   private static String currentScene = null;
    
    @FXML private Button backButton;
    @FXML private Button forwardButton;
    @FXML private Button homeButton;
    
    //private static String currentFxml = null;


    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void pushScene(String fxml) {
    	if (mainStage.getScene() != null) {
            String currentSceneFxml = (String) mainStage.getScene().getUserData();
            if (currentSceneFxml != null) {
                historyStack.push(currentSceneFxml);
            }
        }
        forwardStack.clear();
        changeScene(fxml);
    }

    public static void changeScene(String fxml) {
        try {
            FXMLLoader mainLoader = new FXMLLoader(HeaderController.class.getResource("/views/MainLayout.fxml"));
            Parent mainRoot = mainLoader.load();

            FXMLLoader viewLoader = new FXMLLoader(HeaderController.class.getResource("/views/" + fxml));
            Parent viewRoot = viewLoader.load();

            // Save the current view for tracking
            MainLayoutController layoutController = mainLoader.getController();
            layoutController.setContent(viewRoot);

            double currentWidth = mainStage.getWidth();
            double currentHeight = mainStage.getHeight();

            Scene newScene = new Scene(mainRoot);
            newScene.setUserData(fxml);
            mainStage.setScene(newScene);
            mainStage.setWidth(currentWidth);
            mainStage.setHeight(currentHeight);
            mainStage.show();
            
            currentScene = fxml;

            HeaderController header = layoutController.getHeaderController();
            if (header != null) {
            	header.updateNavigationButtons();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void initialize() {
        //updateNavigationButtons();
    }

    @FXML
    private void handleGoHome(ActionEvent event) {
        if (!isCurrentScene("Home.fxml")) {
            
            historyStack.clear();
            forwardStack.clear();
            currentScene = "Home.fxml";
            changeScene("Home.fxml");
            //updateNavigationButtons();
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
    	if (!historyStack.isEmpty()) {
            String previous = historyStack.pop();
            forwardStack.push(getCurrentSceneFxml());
            changeScene(previous);
        }
    }

    @FXML
    public void handleForward(ActionEvent event) {
    	if (!forwardStack.isEmpty()) {
            String next = forwardStack.pop();
            historyStack.push(getCurrentSceneFxml());
            changeScene(next);
        }
    }
    
    private boolean isCurrentScene(String fxmlName) {
        String current = getCurrentSceneFxml();
        return fxmlName.equals(current);
    }
    
    private String getCurrentSceneFxml() {
        return (mainStage.getScene() != null && mainStage.getScene().getUserData() != null)
                ? (String) mainStage.getScene().getUserData()
                : "";
    }

    public void updateNavigationButtons() {
    	String current = getCurrentSceneFxml();
        boolean isHome = isCurrentScene("Home.fxml");
/*
        System.out.println("Current Scene: " + current);
        System.out.println("Is Home: " + isHome);
        System.out.println("Back Stack: " + historyStack);
        System.out.println("Forward Stack: " + forwardStack); */
        backButton.setDisable(historyStack.isEmpty());
        forwardButton.setDisable(forwardStack.isEmpty());
        homeButton.setDisable(isHome);
    }
           
}
