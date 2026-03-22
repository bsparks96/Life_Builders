------------------------------------------------------------
Install Instructions:

- Run Main.java located in /src/application to start the application.

------------------------------------------------------------

/application  
Contains the Main.java file that serves as the entry point for the JavaFX application.  
It sets up the primary stage, loads the initial FXML view (login), and can manage global configuration.

------------------------------------------------------------

/controllers  
Holds all JavaFX **controller classes**, one per FXML screen.  
Each controller contains the logic for handling button clicks, field interactions, and communication with service classes.

Example controllers:
- LoginController.java
- DashboardController.java
- StudentRecordsController.java

------------------------------------------------------------

/models  
Plain Java classes that represent your core data structures such as users, students, and courses.  
These are typically mapped to the backend JSON response data and passed between controllers and services.

------------------------------------------------------------

/services  
Handles API communication and back-end integration.  
This includes logging in users, fetching student records, and posting data.  
Each service class encapsulates a specific domain (e.g., AuthService, StudentService).

------------------------------------------------------------

/utils  
Contains helper classes such as HTTP clients, session trackers, or utilities for parsing dates and JSON responses.  
Designed to be reusable across controllers and services.

------------------------------------------------------------

/views  
Holds all FXML layout files for each screen in the application.  
Each FXML file defines the user interface layout and is paired with a corresponding controller.

Example views:
- Login.fxml
- Dashboard.fxml
- StudentRecords.fxml

------------------------------------------------------------

/resources/css  
Optional directory to store application-wide CSS files used for customizing the JavaFX UI styling.

