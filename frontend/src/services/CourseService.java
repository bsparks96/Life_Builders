package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.CourseDetailsResponse;
import models.CourseEntryRequest;
import utils.ApiConfig;
import utils.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

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
            	    .version(HttpClient.Version.HTTP_1_1)  // ← CRUCIAL FIX
            	    .header("Content-Type", "application/json")
            	    .header("Accept", "application/json")  // optional but recommended
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
}