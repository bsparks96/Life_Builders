package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.CourseDetailsResponse;
import models.CourseEntryRequest;
import models.IterationAttendanceResponse;
import utils.ApiConfig;
import utils.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class CourseService {

    public static CourseDetailsResponse fetchCourseDetails(int courseID) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
            		.uri(URI.create(ApiConfig.BASE_URL + "/api/courseDetails/" + courseID))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body(), CourseDetailsResponse.class);
            } else {
                System.err.println("Failed to fetch course details. Status: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean submitCourseEntry(CourseEntryRequest requestData) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(requestData);

            HttpRequest request = HttpRequest.newBuilder()
            		.uri(URI.create(utils.ApiConfig.BASE_URL + "/api/courseCreate/"))
            	    .version(HttpClient.Version.HTTP_1_1)  
            	    .header("Content-Type", "application/json")
            	    .header("Accept", "application/json")  
            	    .POST(HttpRequest.BodyPublishers.ofString(json))
            	    .build();
            System.out.println("Sending JSON: " + json);
            System.out.println("Request URI: " + request.uri());
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static List<CourseDetailsResponse> fetchAllCourseDetails() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
            		.uri(URI.create(ApiConfig.BASE_URL + "/api/courseDetails/"))
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch all course details: HTTP " + response.statusCode());
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(
                    response.body(),
                    mapper.getTypeFactory().constructCollectionType(List.class, CourseDetailsResponse.class)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static IterationAttendanceResponse fetchIterationAttendance(int iterationID) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/clients/attendance/iteration/" + iterationID))
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch iteration attendance: HTTP " + response.statusCode());
            }

            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(response.body(), IterationAttendanceResponse.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static boolean submitAttendanceUpdates(Map<String, Object> requestBody) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            String json = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/clients/attendance/"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + SessionManager.getToken()) // 🔥 IMPORTANT
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            System.out.println("Sending Attendance JSON: " + json);
            System.out.println("Request URI: " + request.uri());

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status: " + response.statusCode()); 
            System.out.println("Response Body: " + response.body());

            return response.statusCode() == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean createIteration(models.CreateIterationRequest requestData) {

        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            String json = mapper.writeValueAsString(requestData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/createIteration/"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            System.out.println("Create Iteration Request:");
            System.out.println(json);

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println("Response: " + response.body());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return true;
            } else {
                System.err.println("Create iteration failed: HTTP " + response.statusCode());
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
}