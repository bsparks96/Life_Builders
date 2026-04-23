package utils;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import models.CourseDetailsResponse;


public class SyncManager {

    private static volatile boolean running = false;

    private static String lastSyncTimestamp;

    // 30 seconds for testing (can change to 60 later)
    private static final int POLL_INTERVAL_MS = 30_000;

    public static void start() {

        if (running) return;

        running = true;


        lastSyncTimestamp = Instant.now().toString().replace("Z", "");

        System.out.println("SyncManager started. Initial timestamp: " + lastSyncTimestamp);

        
        ThreadPoolManager.submit(() -> {
        	
        	try {
                // Initial delay to allow data load to complete
                System.out.println("SyncManager waiting for initial data load...");
                Thread.sleep(15_000);

            } catch (InterruptedException e) {
                System.out.println("Initial delay interrupted.");
                return;
            }
        	
            while (running) {

                try {
                    syncChanges();
                    
                    Thread.sleep(POLL_INTERVAL_MS);

                } catch (InterruptedException e) {
                    System.out.println("SyncManager interrupted.");
                    running = false;
                } catch (Exception e) {
                    System.out.println("SyncManager error:");
                    e.printStackTrace();
                }
            }
        });
    }

    public static void stop() {
        running = false;
    }

    private static void syncChanges() {

        System.out.println("Polling for changes since: " + lastSyncTimestamp);

        try {
            // TODO: Replace with actual service call
            // Example:
            // ChangeResponse response = SyncService.getChanges(lastSyncTimestamp);

            // Placeholder for now
            System.out.println("Calling /api/changes...");
            
            var response = services.SyncService.getChanges(lastSyncTimestamp);

            if (response != null) {
                processChanges(response);
            }

            // TODO: process response here
            // processChanges(response);

            // TODO: update timestamp ONLY after successful processing
            // lastSyncTimestamp = response.getLatestTimestamp();

        } catch (Exception e) {
            System.out.println("Failed to sync changes.");
            e.printStackTrace();

            // IMPORTANT: Do NOT update timestamp on failure
        }
    }
    
    private static void processChanges(services.SyncService.ChangeResponse response) {

        if (response == null || response.changes == null || response.changes.isEmpty()) {
            System.out.println("No changes to process.");
            return;
        }

        System.out.println("Processing " + response.count + " changes...");

        // Grouping sets
        Set<Integer> iterationIDsToRefresh = new HashSet<>();
        Set<Integer> clientIDsToRefresh = new HashSet<>();
        Set<Integer> courseIDsToRefresh = new HashSet<>();

        boolean refreshStatistics = false;
        boolean refreshUsers = false;
        boolean refreshCourseList = false;
        boolean refreshCourseDetails = false;

        // Step 1: Group changes
        for (services.SyncService.Change change : response.changes) {

            switch (change.entityType) {

                case "Attendance":
                    iterationIDsToRefresh.add(change.entityID);
                    break;

                case "Iteration":
                    iterationIDsToRefresh.add(change.entityID);
                    refreshCourseDetails = true;
                    break;

                case "Client":
                    clientIDsToRefresh.add(change.entityID);
                    break;

                case "Course":
                    courseIDsToRefresh.add(change.entityID);
                    refreshCourseList = true;
                    break;

                case "User":
                    refreshUsers = true;
                    break;

                case "Statistics":
                    refreshStatistics = true;
                    break;

                default:
                    System.out.println("Unknown entityType: " + change.entityType);
            }
        }

        // Step 2: Execute fetches

        // --- Attendance / Iterations ---
        for (int iterationID : iterationIDsToRefresh) {
            try {
                var responseData = services.CourseService.fetchIterationAttendance(iterationID);

                if (responseData != null) {
                    utils.CourseSessionCache.setFromResponse(responseData);
                    System.out.println("Updated attendance for iteration " + iterationID);
                }

            } catch (Exception e) {
                System.out.println("Failed to update iteration " + iterationID);
                e.printStackTrace();
            }
        }

        // --- Clients ---
        for (int clientID : clientIDsToRefresh) {
            try {
                var client = services.ClientService.getClientDetails(clientID);

                if (client != null) {
                    utils.ClientDetailsCache.updateClient(client);
                    System.out.println("Updated client " + clientID);
                }

            } catch (Exception e) {
                System.out.println("Failed to update client " + clientID);
                e.printStackTrace();
            }
        }

        // --- Courses ---
        for (int courseID : courseIDsToRefresh) {
            try {
                var course = services.CourseService.fetchCourseDetails(courseID);

                if (course != null) {
                    utils.CourseCache.updateCourse(course);
                    System.out.println("Updated course " + courseID);
                }

            } catch (Exception e) {
                System.out.println("Failed to update course " + courseID);
                e.printStackTrace();
            }
        }

        // --- Users ---
        if (refreshUsers) {
            try {
                var userMap = services.UserService.fetchAllUsers();
                utils.UserCache.setAvailableUsers(userMap);
                System.out.println("User cache refreshed");
            } catch (Exception e) {
                System.out.println("Failed to refresh users");
                e.printStackTrace();
            }
        }

        // --- Statistics ---
        if (refreshStatistics) {
            try {
                boolean success = services.StatisticsService.fetchStatistics();
                if (success) {
                    System.out.println("Statistics refreshed");
                }
            } catch (Exception e) {
                System.out.println("Failed to refresh statistics");
                e.printStackTrace();
            }
        }
        
        if (refreshCourseList) {
            try {
                var courseMap = services.ClientService.fetchAllCourses();
                Platform.runLater(() -> {
                    utils.CourseCache.setAvailableCourses(courseMap);
                });
                System.out.println("Course list refreshed");
            } catch (Exception e) {
                System.out.println("Failed to refresh course list");
                e.printStackTrace();
            }
        }
        
        if (refreshCourseDetails) {
            try {
                List<CourseDetailsResponse> courses = services.CourseService.fetchAllCourseDetails();

                if (courses != null) {
                	Platform.runLater(() -> {
                	    utils.CourseCache.setCourseDetails(courses);
                	});
                    System.out.println("Course details refreshed (iteration change)");
                }

            } catch (Exception e) {
                System.out.println("Failed to refresh course details");
                e.printStackTrace();
            }
        }

        // Step 3: Update timestamp ONLY after success
        lastSyncTimestamp = response.latestTimestamp;

        System.out.println("Sync complete. New timestamp: " + lastSyncTimestamp);
    }
    
    
    
}