package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.StatisticsResponse;
import utils.ApiConfig;
import utils.SessionManager;
import utils.StatisticsCache;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StatisticsService {

    public static boolean fetchStatistics() {

        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/api/statistics/"))
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Failed to fetch statistics: HTTP " + response.statusCode());
                return false;
            }

            StatisticsResponse stats =
                    mapper.readValue(response.body(), StatisticsResponse.class);

            StatisticsCache.setStatistics(stats);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}