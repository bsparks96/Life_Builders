package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.UserDetails;
import models.UserUpdateRequest;
import utils.ApiConfig;
import utils.SessionManager;
import models.ResetPasswordResponse;
import models.UserCreateRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

public class UserService {

	private static final String API_URL = utils.ApiConfig.BASE_URL + "/api/users";

    public static Map<String, Integer> fetchAllUsers() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization",  "Bearer " + SessionManager.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch users: HTTP " + response.statusCode());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode userArray = mapper.readTree(response.body());

        Map<String, Integer> nameToIdMap = new HashMap<>();

        for (JsonNode user : userArray) {
            int userID = user.get("userID").asInt();
            String firstName = user.get("firstName").asText();
            String lastName = user.get("lastName").asText();
            String fullName = firstName + " " + lastName;
            nameToIdMap.put(fullName, userID);
        }

        return nameToIdMap;
    }
    
    public static List<UserDetails> fetchUserDetailsBulk(List<Integer> userIDs) {

        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            String jsonBody = mapper.writeValueAsString(new UserIDRequest(userIDs));
            System.out.println("Request Body: " + jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/users/bulk/"))
                    .version(HttpClient.Version.HTTP_1_1) 
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Failed to fetch user details: HTTP " + response.statusCode());
                return null;
            }

            UserDetails[] users = mapper.readValue(response.body(), UserDetails[].class);
            
            return Arrays.asList(users);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static class UserIDRequest {
        public List<Integer> userIDs;
        
        public UserIDRequest() {}

        public UserIDRequest(List<Integer> userIDs) {
            this.userIDs = userIDs;
        }
    }
    
    public static Integer createUser(UserCreateRequest req) {

        try {
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            ObjectMapper mapper = new ObjectMapper();

            String json = mapper.writeValueAsString(req);

            System.out.println("Create User JSON: " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/users/create/"))
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            if (response.statusCode() != 200) {
                return null;
            }

            JsonNode node = mapper.readTree(response.body());

            if (node.has("userID")) {
                return node.get("userID").asInt();
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    

    public static boolean updateUser(UserUpdateRequest requestData) {

        try {
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            ObjectMapper mapper = new ObjectMapper();

            mapper.setSerializationInclusion(
                    com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
            );

            String json = mapper.writeValueAsString(requestData);

            System.out.println("Update User JSON: " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/users/update/"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)) // 🔥 FIXED
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            return response.statusCode() == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static ResetPasswordResponse resetPassword(int userID) {

        try {
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            ObjectMapper mapper = new ObjectMapper();

            String json = mapper.writeValueAsString(
                    java.util.Map.of("userID", userID)
            );

            System.out.println("Reset Password JSON: " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/users/reset-password/"))
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            if (response.statusCode() != 200) {
                return null;
            }

            return mapper.readValue(response.body(), ResetPasswordResponse.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
