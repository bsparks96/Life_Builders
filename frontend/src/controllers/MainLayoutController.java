package controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import models.ClientDetailsResponse;
import models.CourseDetailsResponse;
import models.IterationAttendanceResponse;
import models.IterationOut;
import services.ClientService;
import services.CourseService;
import services.UserService;
import utils.ClientDetailsCache;
import utils.CourseCache;
import utils.CourseSessionCache;
import javafx.scene.Parent;
import javafx.scene.Node;

public class MainLayoutController {

    @FXML
    private StackPane contentArea;

    
    @FXML
    private HeaderController headerControllerInstance;
    @FXML
    private HBox headerController;

  
    
    public HeaderController getHeaderController() {
    	return headerControllerInstance;
    }
    
    
    @FXML
    public void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Header.fxml"));
            HBox header = loader.load();
            headerControllerInstance = loader.getController();
            this.headerController.getChildren().setAll(header.getChildren());

            // Start background data loading
            loadDataInBackground();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    
    public void setContent(Node content) {
        contentArea.getChildren().setAll(content);

        // Try updating header navigation buttons
        // Use lookup to find the controller manually
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Header.fxml"));
        try {
            loader.load();
            HeaderController controller = loader.getController();
            //controller.updateNavigationButtons();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadDataInBackground() {
        new Thread(() -> {
            try {

                // Load courses
                if (CourseCache.getCourseNames().isEmpty()) {
                    Map<String, Integer> courseMap = ClientService.fetchAllCourses();
                    CourseCache.setAvailableCourses(courseMap);
                    System.out.println("Courses loaded");
                }

                // Load users
                if (utils.UserCache.getUserNames().isEmpty()) {
                    Map<String, Integer> userMap = UserService.fetchAllUsers();
                    utils.UserCache.setAvailableUsers(userMap);
                    System.out.println("Users loaded");
                }

                // Load client details
                if (!ClientDetailsCache.isLoaded()) {
                    List<ClientDetailsResponse> clients = ClientService.fetchAllClientDetails();
                    ClientDetailsCache.setAllClientDetails(clients);
                    System.out.println("Client details loaded");
                }
                
                if (!CourseCache.isDetailsLoaded()) {
                    List<CourseDetailsResponse> courses = CourseService.fetchAllCourseDetails();
                    if (courses != null) {
                        CourseCache.setCourseDetails(courses);
                        System.out.println("Course details loaded into cache");
                    }
                }
                
                if (CourseCache.isDetailsLoaded()) {

                    for (CourseDetailsResponse course : CourseCache.getAllCourseDetails()) {

                        if (course.getIterations() == null) continue;

                        for (IterationOut iteration : course.getIterations()) {

                            int iterationID = iteration.getIterationID(); 

                            if (!CourseSessionCache.isLoaded(iterationID)) {

                                IterationAttendanceResponse response =
                                        CourseService.fetchIterationAttendance(iterationID);

                                if (response != null) {
                                    CourseSessionCache.setFromResponse(response);
                                    System.out.println("Loaded attendance for iteration " + iterationID);
                                    // debugSpecificAttendance(iterationID);
                                }
                            }
                        }
                    }
                }
                
                
                
                System.out.println("All background data loaded");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    
    
}