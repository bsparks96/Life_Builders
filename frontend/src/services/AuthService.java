package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.ApiConfig;
import utils.SessionManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class AuthService {

    private static final String LOGIN_URL = ApiConfig.BASE_URL + "/api/users/login";
    private static final String ME_URL = ApiConfig.BASE_URL + "/api/users/me";


    public static boolean login(String username, String password) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(new LoginRequest(username, password));


            URL url = new URL(LOGIN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int status = conn.getResponseCode();
            InputStream responseStream = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            StringBuilder responseBody = new StringBuilder();
            try (Scanner scanner = new Scanner(responseStream)) {
                while (scanner.hasNext()) {
                    responseBody.append(scanner.nextLine());
                }
            }

            if (status == 200) {
            	JsonNode json = mapper.readTree(responseBody.toString());

                String token = json.get("access_token").asText();

                boolean mustChange = json.has("mustChangePassword") &&
                                     json.get("mustChangePassword").asBoolean();

                SessionManager.setToken(token);
                SessionManager.setMustChangePassword(mustChange);
                return true;
            } else {
                System.err.println("Login failed: " + responseBody);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean fetchCurrentUser() {
        try {
            String token = SessionManager.getToken();
            if (token == null) return false;

            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ME_URL))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = mapper.readTree(response.body());

                String username = json.get("sub").asText();
                int userID = json.get("userID").asInt();
                String role = json.get("role").asText();

                SessionManager.setUser(username, userID, role);

                return true;
            } else if (response.statusCode() == 401) {
                SessionManager.clearSession();
                return false;
            } else {
                System.err.println("Failed to fetch user: " + response.body());
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean changePassword(String currentPassword, String newPassword) {

        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            String json = mapper.writeValueAsString(
                    java.util.Map.of(
                            "currentPassword", currentPassword,
                            "newPassword", newPassword
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/users/change-password/"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json)) 
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static class LoginRequest {
        public String username;
        public String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}