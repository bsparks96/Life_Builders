package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.ApiConfig;
import utils.SessionManager;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SyncService {

    public static class Change {
        public String entityType;
        public int entityID;
        public String operationType;
        public String updatedAt;
        public Integer updatedBy;
    }

    public static class ChangeResponse {
        public List<Change> changes;
        public String latestTimestamp;
        public int count;
    }

    public static ChangeResponse getChanges(String sinceTimestamp) {

        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();


            String encodedTimestamp = URLEncoder.encode(sinceTimestamp, StandardCharsets.UTF_8);
            String url = ApiConfig.BASE_URL + "/api/changes/?since=" + encodedTimestamp;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .GET()
                    .build();

            System.out.println("Fetching changes from: " + url);

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Failed to fetch changes: HTTP " + response.statusCode());
                System.err.println(response.body());
                return null;
            }

            JsonNode root = mapper.readTree(response.body());

            ChangeResponse result = new ChangeResponse();
            result.changes = new ArrayList<>();
            result.latestTimestamp = root.get("latestTimestamp").asText();
            result.count = root.get("count").asInt();

            for (JsonNode node : root.get("changes")) {
                Change c = new Change();
                c.entityType = node.get("entityType").asText();
                c.entityID = node.get("entityID").asInt();
                c.operationType = node.get("operationType").asText();
                c.updatedAt = node.get("updatedAt").asText();
                c.updatedBy = node.has("updatedBy") ? node.get("updatedBy").asInt() : null;

                result.changes.add(c);
            }

            return result;

        } catch (Exception e) {
            System.err.println("Error fetching changes:");
            e.printStackTrace();
            return null;
        }
    }
    
    
}