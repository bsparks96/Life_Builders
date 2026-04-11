package controllers;

import javafx.fxml.FXML;
import utils.ClientDetailsCache;
import utils.CourseCache;
import utils.CourseSessionCache;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.ToggleButton;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import models.ClientDetailsResponse;
import models.CourseDetailsResponse;
import models.CourseEntryRequest;
import services.ClientService;
import services.CourseService;
import models.InstructorOut;
import models.IterationAttendanceResponse;
import models.IterationOut;


public class CourseManagementController {

    @FXML private ListView<String> courseListView;
    @FXML private StackPane detailsPane;
    @FXML private Button newCourseButton;
    @FXML private TextField courseNameField;
    @FXML private TextField instructorField;
    @FXML private TextField courseLengthField;
    @FXML private ListView<String> instructorListView;
    @FXML private ComboBox<String> instructorCombo;


    @FXML private VBox courseDetailPane;
    @FXML private VBox iterationsBox;
    private VBox attendanceSection;
    private GridPane attendanceGrid;
    @FXML private DatePicker iterationStartPicker;
    @FXML private DatePicker iterationEndPicker;
    @FXML private DatePicker sessionCalendar;

    @FXML private ToggleButton monBtn;
    @FXML private ToggleButton tueBtn;
    @FXML private ToggleButton wedBtn;
    @FXML private ToggleButton thuBtn;
    @FXML private ToggleButton friBtn;
    @FXML private ToggleButton satBtn;
    @FXML private ToggleButton sunBtn;

    private Set<LocalDate> sessionDates = new HashSet<>();
    private Map<String, Boolean> attendanceChanges = new HashMap<>();

    private GridPane headerGridRef;
    private VBox nameColumnRef;

    @FXML
    public void initialize() {

    	
    	courseListView.setItems(CourseCache.getCourseNames());
    	
        courseListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showCourseDetails(newVal);
            }
        });
        
        iterationStartPicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (iterationEndPicker.getValue() == null && newDate != null) {
                String weeksStr = courseLengthField.getText();
                try {
                    int weeks = Integer.parseInt(weeksStr);
                    iterationEndPicker.setValue(newDate.plusWeeks(weeks));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid course length format.");
                }
            }
        });
        
        
        instructorListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        instructorListView.getItems().addAll(utils.UserCache.getUserNames());
        instructorCombo.setItems(FXCollections.observableArrayList(utils.UserCache.getUserNames()));
        
        
        monBtn.setOnAction(e -> generateSessionDates());
        tueBtn.setOnAction(e -> generateSessionDates());
        wedBtn.setOnAction(e -> generateSessionDates());
        thuBtn.setOnAction(e -> generateSessionDates());
        friBtn.setOnAction(e -> generateSessionDates());
        satBtn.setOnAction(e -> generateSessionDates());
        sunBtn.setOnAction(e -> generateSessionDates());

        iterationStartPicker.setOnAction(e -> generateSessionDates());
        iterationEndPicker.setOnAction(e -> generateSessionDates());

    }

    @FXML
    private void sortAlphabetically() {
        FXCollections.sort(courseListView.getItems(), String::compareToIgnoreCase);
    }

    @FXML
    private void sortMostRecent() {
        // TODO: Sort the list by recent dates
    }

    @FXML
    private void handleAddIteration(ActionEvent event) {
        HBox iterationRow = new HBox(10);

        DatePicker newStartPicker = new DatePicker();
        newStartPicker.setPromptText("Start Date");
        newStartPicker.setMaxWidth(150);

        DatePicker newEndPicker = new DatePicker();
        newEndPicker.setPromptText("End Date");
        newEndPicker.setMaxWidth(150);

        // Listener: auto-set end date based on course length if empty
        newStartPicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newEndPicker.getValue() == null && newDate != null) {
                try {
                    int weeks = Integer.parseInt(courseLengthField.getText());
                    newEndPicker.setValue(newDate.plusWeeks(weeks));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid course length format.");
                }
            }
        });

        Button removeBtn = new Button("Remove");
        removeBtn.setOnAction(e -> iterationsBox.getChildren().remove(iterationRow));

        iterationRow.getChildren().addAll(newStartPicker, newEndPicker, removeBtn);
        iterationsBox.getChildren().add(iterationRow);
    }
    
    @FXML
    private void addInstructor(ActionEvent event) {
        String selected = instructorCombo.getValue();
        if (selected != null && !selected.isBlank() && !instructorListView.getItems().contains(selected)) {
            instructorListView.getItems().add(selected);
        }
    }

    
    @FXML
    private void handleSaveCourse(ActionEvent event) {
        try {
            CourseEntryRequest req = new CourseEntryRequest();
            req.courseName = courseNameField.getText().trim();

            try {
                req.courseLength = Integer.parseInt(courseLengthField.getText().trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Course length must be a number.");
                return;
            }

            req.instructorIDs = new ArrayList<>();
            for (String instructorName : instructorListView.getItems()) {
                Integer id = utils.UserCache.getUserID(instructorName);
                if (id != null) {
                    req.instructorIDs.add(id);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Invalid instructor: " + instructorName);
                    return;
                }
            }

            req.iterations = new ArrayList<>();
            for (Node node : iterationsBox.getChildren()) {
                if (node instanceof HBox row) {
                    List<Node> inputs = row.getChildren();

                    if (inputs.size() >= 2 &&
                        inputs.get(0) instanceof DatePicker start &&
                        inputs.get(1) instanceof DatePicker end) {

                        CourseEntryRequest.IterationIn iter = new CourseEntryRequest.IterationIn();

                        iter.courseStartDate = start.getValue() != null ? start.getValue().toString() : null;
                        iter.courseEndDate = end.getValue() != null ? end.getValue().toString() : null;

                        iter.sessions = getSessionDateStrings();

                        if (iter.courseStartDate != null && iter.courseEndDate != null) {
                            req.iterations.add(iter);
                        }
                    }
                }
            }

            System.out.println("Request Body:");
            System.out.println("Course Name: " + req.courseName);
            System.out.println("Course Length: " + req.courseLength);
            System.out.println("Instructor IDs: " + req.instructorIDs);
            System.out.println("Iterations:");

            for (CourseEntryRequest.IterationIn iter : req.iterations) {
                System.out.println("- Start: " + iter.courseStartDate + ", End: " + iter.courseEndDate);

                if (iter.sessions != null) {
                    System.out.println("  Sessions:");
                    for (String s : iter.sessions) {
                        System.out.println("   - " + s);
                    }
                }
            }

            boolean success = CourseService.submitCourseEntry(req);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Course created successfully!");

                courseDetailPane.setVisible(false);
                courseDetailPane.setManaged(false);

                sessionDates.clear();
                refreshCourseCache();

            } else {
                showAlert(Alert.AlertType.ERROR, "Failed to create course.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "An unexpected error occurred.");
        }
    }
    
    
    private void showAlert(Alert.AlertType type, String msg) {
    	Alert alert = new Alert(type, msg);
        alert.showAndWait();
		
	}

    @FXML
    private void handleNewCourseClick(ActionEvent event) {

    	System.out.println("New Course clicked");
        courseNameField.clear();
        instructorListView.getItems().clear();
        courseLengthField.clear();
        iterationStartPicker.setValue(null);
        iterationEndPicker.setValue(null);
        courseDetailPane.setVisible(true);
        courseDetailPane.setManaged(true);
        instructorCombo.setValue(null);
        detailsPane.getChildren().removeIf(node -> node != courseDetailPane);
    }
    

    private void showCourseDetails(String courseName) {
    	Integer courseID = CourseCache.getCourseID(courseName);
        if (courseID == null) {
            System.err.println("Course ID not found for course: " + courseName);
            return;
        }

        try {
        	CourseDetailsResponse details = CourseCache.getCourseDetails(courseID);

        	if (details == null) {
        	    // fallback if cache isn't ready yet
        	    try {
        	        details = CourseService.fetchCourseDetails(courseID);
        	    } catch (Exception e) {
        	        e.printStackTrace();
        	        return;
        	    }
        	}

            VBox detailBox = new VBox(10);
            detailBox.getChildren().add(new Text(courseName + ":"));

            // Instructors
            StringBuilder instructorList = new StringBuilder("Instructors:\n");
            for (InstructorOut instructor : details.getInstructors()) {
            	String instructorName = utils.UserCache.getUserNameByID(instructor.getUserID());
            	instructorList.append("- ").append(instructorName).append("\n");
            }
            detailBox.getChildren().add(new Label(instructorList.toString()));

            /*
            StringBuilder iterationList = new StringBuilder("Current Iterations:\n");
            for (IterationOut iteration : details.getIterations()) {
                iterationList.append("- ").append(iteration.getStartDate()).append(" to ").append(iteration.getEndDate()).append("\n");
            }
            detailBox.getChildren().add(new Label(iterationList.toString()));
			*/
            
            detailBox.getChildren().add(new Label("Current Iterations:"));

	        VBox iterationContainer = new VBox(5);
	        
	        attendanceSection = new VBox(10);
	        attendanceSection.setVisible(false);
	        attendanceSection.setManaged(false);

	        // Grid
	        attendanceGrid = new GridPane();
	        attendanceGrid.setHgap(10);
	        attendanceGrid.setVgap(5);

	        // Buttons
	        HBox buttonRow = new HBox(10);
	        Button updateBtn = new Button("Update");
	        updateBtn.setOnAction(e -> handleUpdateAttendance());
	        
	        for (IterationOut iteration : details.getIterations()) {

	            int iterationID = iteration.getIterationID(); // ✅ NOW IN SCOPE

	            HBox iterationRow = new HBox(10);

	            Label iterationLabel = new Label(
	                iteration.getStartDate() + " to " + iteration.getEndDate()
	            );

	            Button viewClientsBtn = new Button("View Clients");
	            

	            viewClientsBtn.setOnAction(e -> {

	                System.out.println("Loading grid for iteration: " + iterationID);

	                attendanceSection.setVisible(true);
	                attendanceSection.setManaged(true);

	                loadAttendanceGrid(attendanceGrid, iterationID);
	            });

	            // ✅ ADD CLIENTS BUTTON HERE
	            Button addClientsBtn = new Button("Add Clients");
	            buttonRow.getChildren().addAll(updateBtn, addClientsBtn);
	            addClientsBtn.setOnAction(e -> {
	                openAddClientsDialog(iterationID, courseID);
	            });

	            iterationRow.getChildren().addAll(iterationLabel, viewClientsBtn, addClientsBtn);

	            iterationContainer.getChildren().add(iterationRow);
	        } 

	        headerGridRef = new GridPane();
	        GridPane headerGrid = headerGridRef;
	        headerGrid.setHgap(10);

	        ScrollPane headerScroll = new ScrollPane(headerGrid);
	        headerScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
	        headerScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
	        headerScroll.setFitToHeight(true);
	        headerScroll.setFitToWidth(false);
	        
	        headerScroll.setPadding(Insets.EMPTY);
	        headerScroll.setStyle("-fx-padding: 0;");

	        HBox mainContainer = new HBox(5);

	        nameColumnRef = new VBox();
	        VBox nameColumn = nameColumnRef;
	        nameColumn.setMinWidth(Region.USE_PREF_SIZE);
	        nameColumn.setPrefWidth(Region.USE_COMPUTED_SIZE);
	        nameColumn.setMaxWidth(Region.USE_PREF_SIZE);
	        nameColumn.setSpacing(5);
	        nameColumn.setStyle("-fx-padding: 0 5 0 0;");

	        attendanceGrid = new GridPane();
	        attendanceGrid.setHgap(10);
	        attendanceGrid.setVgap(8);

	        ScrollPane gridScroll = new ScrollPane(attendanceGrid);
	        gridScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
	        gridScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
	        gridScroll.prefViewportWidthProperty().bind(
	        	    detailsPane.widthProperty().subtract(100) // 200 name column + padding
	        	);
	        headerScroll.prefViewportWidthProperty().bind(
	        	    gridScroll.prefViewportWidthProperty()
	        	);

	        gridScroll.setFitToHeight(true);
	        gridScroll.setFitToWidth(false);
	        gridScroll.setPadding(Insets.EMPTY);
	        gridScroll.setStyle("-fx-padding: 0;");

	        headerScroll.hvalueProperty().bindBidirectional(gridScroll.hvalueProperty());

	        mainContainer.getChildren().addAll(nameColumn, gridScroll);

	        HBox headerContainer = new HBox(2);

		     // empty spacer for name column alignment
		    Region spacer = new Region();
		    spacer.prefWidthProperty().bind(nameColumn.widthProperty());
	
		    headerContainer.getChildren().addAll(spacer, headerScroll);
	
		    attendanceSection.getChildren().addAll(headerContainer, mainContainer, buttonRow);
		     
	        detailBox.getChildren().add(iterationContainer);
	        
	        
	        detailBox.getChildren().add(attendanceSection);

            detailBox.getChildren().add(new Label("Past Iterations: "));



            courseDetailPane.setVisible(false);
            courseDetailPane.setManaged(false);

            // Remove any previous detail views (but NOT the form)
            detailsPane.getChildren().removeIf(node -> node != courseDetailPane);

            // Add the new detail view
            detailsPane.getChildren().add(detailBox);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching course details for ID: " + courseID);
        }
    }
    
    private void generateSessionDates() {

        sessionDates.clear();

        LocalDate start = iterationStartPicker.getValue();
        LocalDate end = iterationEndPicker.getValue();

        if (start == null || end == null || end.isBefore(start)) return;

        Set<DayOfWeek> selectedDays = new HashSet<>();

        if (monBtn.isSelected()) selectedDays.add(DayOfWeek.MONDAY);
        if (tueBtn.isSelected()) selectedDays.add(DayOfWeek.TUESDAY);
        if (wedBtn.isSelected()) selectedDays.add(DayOfWeek.WEDNESDAY);
        if (thuBtn.isSelected()) selectedDays.add(DayOfWeek.THURSDAY);
        if (friBtn.isSelected()) selectedDays.add(DayOfWeek.FRIDAY);
        if (satBtn.isSelected()) selectedDays.add(DayOfWeek.SATURDAY);
        if (sunBtn.isSelected()) selectedDays.add(DayOfWeek.SUNDAY);

        LocalDate current = start;

        while (!current.isAfter(end)) {
            if (selectedDays.contains(current.getDayOfWeek())) {
                sessionDates.add(current);
            }
            current = current.plusDays(1);
        }

        updateCalendarHighlighting();
    }
    
    private void updateCalendarHighlighting() {
        sessionCalendar.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (empty) return;

                if (sessionDates.contains(date)) {
                    setStyle("-fx-background-color: lightgreen;");
                } else {
                    setStyle("");
                }

                setOnMouseClicked(event -> {
                    if (sessionDates.contains(date)) {
                        sessionDates.remove(date);
                    } else {
                        sessionDates.add(date);
                    }

                    sessionCalendar.getEditor().setText(sessionCalendar.getEditor().getText());
                });
            }
        });
    }
    
    private List<String> getSessionDateStrings() {
        List<LocalDate> sorted = new ArrayList<>(sessionDates);
        sorted.sort(LocalDate::compareTo);

        List<String> result = new ArrayList<>();
        for (LocalDate d : sorted) {
            result.add(d.toString()); 
        }
        return result;
    }
    
    private void refreshCourseCache() {
        try {
            Map<String, Integer> courseMap = ClientService.fetchAllCourses();
            CourseCache.setAvailableCourses(courseMap);

            courseListView.setItems(CourseCache.getCourseNames());

            System.out.println("Course cache refreshed");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to refresh course cache");
        }
    }
    
    private void loadAttendanceGrid(GridPane grid, int iterationID) {

        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        var sessions = utils.CourseSessionCache.getSessions(iterationID);
        var clients = utils.CourseSessionCache.getClients(iterationID);

        if (sessions == null || clients == null) return;

        GridPane headerGrid = headerGridRef;
        VBox nameColumn = nameColumnRef;

        nameColumn.getChildren().clear();

        ColumnConstraints sessionCol = new ColumnConstraints();
        sessionCol.setPrefWidth(80);
        sessionCol.setMinWidth(60);

        headerGrid.getChildren().clear();
        headerGrid.getColumnConstraints().clear();
        grid.getColumnConstraints().clear(); 

        for (int i = 0; i < sessions.size(); i++) {

            ColumnConstraints gridCol = new ColumnConstraints();
            gridCol.setPrefWidth(80);
            gridCol.setMinWidth(60);
            grid.getColumnConstraints().add(gridCol);

            ColumnConstraints headerCol = new ColumnConstraints();
            headerCol.setPrefWidth(80);
            headerCol.setMinWidth(60);
            headerGrid.getColumnConstraints().add(headerCol);
        }

        for (int col = 0; col < sessions.size(); col++) {
            Label dateLabel = new Label(sessions.get(col).date);
            dateLabel.setStyle("-fx-font-weight: bold;");
            headerGrid.add(dateLabel, col, 0);
        }

        for (int row = 0; row < clients.size(); row++) {

            var client = clients.get(row);

            Label nameLabel = new Label(client.name);
            nameLabel.setMinWidth(Region.USE_PREF_SIZE);
            nameColumn.getChildren().add(nameLabel);

            for (int col = 0; col < sessions.size(); col++) {

                var session = sessions.get(col);

                boolean attended = utils.CourseSessionCache.getAttendance(
                        iterationID,
                        client.clientID,
                        session.sessionID
                );

                CheckBox cb = new CheckBox();
                cb.setSelected(attended);

                int clientID = client.clientID;
                int sessionID = session.sessionID;

                cb.setOnAction(e -> {

                    boolean newValue = cb.isSelected();

                    utils.CourseSessionCache.updateAttendance(
                            iterationID,
                            clientID,
                            sessionID,
                            newValue
                    );

                    String key = iterationID + "-" + clientID + "-" + sessionID;
                    attendanceChanges.put(key, newValue);
                });

                grid.add(cb, col, row + 1);
            }
        }
        
        grid.setAlignment(Pos.CENTER_LEFT);

        // =========================
        // 🔷 SPACING / STYLE
        // =========================

        grid.setHgap(10);
        nameColumn.setSpacing(8);
        grid.setVgap(8);
    }

    private void handleUpdateAttendance() {

        if (attendanceChanges.isEmpty()) {
            System.out.println("No changes to update.");
            return;
        }

        List<Map<String, Object>> updates = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : attendanceChanges.entrySet()) {

            String[] parts = entry.getKey().split("-");

            int iterationID = Integer.parseInt(parts[0]); // not needed for API but kept for clarity
            int clientID = Integer.parseInt(parts[1]);
            int sessionID = Integer.parseInt(parts[2]);

            Map<String, Object> update = new HashMap<>();
            update.put("sessionID", sessionID);
            update.put("clientID", clientID);
            update.put("attendance", entry.getValue());

            updates.add(update);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("updates", updates);

        try {
            boolean success = CourseService.submitAttendanceUpdates(requestBody);

            if (success) {
                System.out.println("Attendance updated successfully!");

                // Clear changes after success
                attendanceChanges.clear();

            } else {
                System.out.println("Failed to update attendance.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openAddClientsDialog(int iterationID, int courseID) {

        Stage stage = new Stage();
        stage.setTitle("Add Clients");

        VBox root = new VBox(10);
        root.setPrefSize(350, 450);

        // 🔷 ListView now holds full objects
        ListView<ClientDetailsResponse> clientList = new ListView<>();

        // 🔷 Get already enrolled clients for this iteration
        List<CourseSessionCache.ClientAttendance> enrolledClients =
                CourseSessionCache.getClients(iterationID);

        Set<Integer> enrolledClientIDs = new HashSet<>();
        for (CourseSessionCache.ClientAttendance c : enrolledClients) {
            enrolledClientIDs.add(c.clientID);
        }

        // 🔷 Build selectable client list (exclude already enrolled)
        ObservableList<ClientDetailsResponse> clients = FXCollections.observableArrayList();
        Map<ClientDetailsResponse, BooleanProperty> selectionMap = new HashMap<>();

        for (ClientDetailsResponse client : ClientDetailsCache.getAllClients().values()) {

            // ❌ Skip already enrolled clients
            if (enrolledClientIDs.contains(client.getClientID())) continue;

            clients.add(client);
            selectionMap.put(client, new SimpleBooleanProperty(false));
        }

        clientList.setItems(clients);

        // 🔷 Checkbox UI
        clientList.setCellFactory(listView -> new CheckBoxListCell<ClientDetailsResponse>(client -> selectionMap.get(client)) {
            @Override
            public void updateItem(ClientDetailsResponse item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    setText(item.getFullName());
                } else {
                    setText(null);
                }
            }
        });

        // 🔷 Enroll button
        Button enrollBtn = new Button("Enroll Selected");

        enrollBtn.setOnAction(e -> {

            for (Map.Entry<ClientDetailsResponse, BooleanProperty> entry : selectionMap.entrySet()) {

                if (entry.getValue().get()) {

                    ClientDetailsResponse client = entry.getKey();

                    boolean success = ClientService.enrollClient(
                            client.getClientID(),
                            courseID,
                            iterationID
                    );

                    if (!success) {
                        System.out.println("Failed to enroll: " + client.getFullName());
                    }
                }
            }

            // 🔷 Refresh attendance after enrollment
            IterationAttendanceResponse response =
                    CourseService.fetchIterationAttendance(iterationID);

            if (response != null) {
                CourseSessionCache.setFromResponse(response);
                loadAttendanceGrid(attendanceGrid, iterationID);
            }

            stage.close();
        });

        // 🔷 Optional UX: message if no clients available
        if (clients.isEmpty()) {
            root.getChildren().add(new Label("All clients are already enrolled."));
        }

        root.getChildren().addAll(clientList, enrollBtn);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
}