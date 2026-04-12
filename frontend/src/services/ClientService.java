package services;

import models.Client;
import models.ClientDetailsResponse;
import models.ClientEntryRequest;
import models.CompletionUpdateRequest;
import utils.CourseCache;
import utils.SessionManager;
import utils.ApiConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ClientService {

    private static final String API_URL = utils.ApiConfig.BASE_URL + "/api/clients";

    public static List<Client> fetchAllClients() throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        Scanner sc = new Scanner(conn.getInputStream());
        StringBuilder json = new StringBuilder();
        while (sc.hasNext()) {
            json.append(sc.nextLine());
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json.toString(), new TypeReference<List<Client>>() {});
    }
    
    
    public static Map<String, Integer> fetchAllCourses() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
        		.uri(URI.create(utils.ApiConfig.BASE_URL + "/api/courses/"))  // adjust if needed
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        Map<String, Integer> nameToIdMap = new HashMap<>();

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode courseArray = mapper.readTree(response.body());

            for (JsonNode course : courseArray) {
                int courseID = course.get("courseID").asInt();
                String courseName = course.get("courseName").asText();
                nameToIdMap.put(courseName, courseID);
                CourseCache.courseMap.put(courseID, courseName);
                System.out.println("Course ID: " + courseID + ", Name: " + courseName);
            }
        } else {
            System.err.println("Failed to fetch courses. Status: " + response.statusCode());
        }
        
        return nameToIdMap;
    }
    
    public static List<ClientDetailsResponse> fetchAllClientDetails() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/clients/details/"))
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch all client details: HTTP " + response.statusCode());
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(),
                    mapper.getTypeFactory().constructCollectionType(List.class, ClientDetailsResponse.class));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    public static boolean submitClientEntry(ClientEntryRequest requestData) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(requestData);

            HttpRequest request = HttpRequest.newBuilder()
            	.uri(URI.create(utils.ApiConfig.BASE_URL + "/api/clients/clientEntry/"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return true;
            } else {
                System.err.println("Failed to submit client entry. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static ClientDetailsResponse getClientDetails(int clientID) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/clients/details/" + clientID))
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch client details: HTTP " + response.statusCode());
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), ClientDetailsResponse.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    public static boolean enrollClient(int clientID, int courseID, int iterationID) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> body = new HashMap<>();
            body.put("clientID", clientID);
            body.put("courseID", courseID);
            body.put("iterationID", iterationID);

            String json = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/clients/enroll/"))
                    .version(HttpClient.Version.HTTP_1_1)   // ✅ ADD THIS
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")   // ✅ ADD THIS
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            System.out.println("Sending Enroll JSON: " + json);

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Enroll Status: " + response.statusCode());
            System.out.println("Enroll Response: " + response.body());

            return response.statusCode() == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean updateCompletion(List<CompletionUpdateRequest.CompletionRecord> updates) {

        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            CompletionUpdateRequest requestBody = new CompletionUpdateRequest();
            requestBody.updates = updates;

            String json = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/clients/completion/"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            System.out.println("Completion Request JSON: " + json);

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Completion Status: " + response.statusCode());
            System.out.println("Completion Response: " + response.body());

            return response.statusCode() == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
}
