package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.StatisticsResponse;
import models.StatisticsRangeResponse;
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
    
    
    public static models.StatisticsRangeResponse fetchRange(String startDate, String endDate) {

        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            String url = ApiConfig.BASE_URL + "/api/statistics/range/"
                    + "?startDate=" + startDate
                    + "&endDate=" + endDate;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + SessionManager.getToken())
                    .GET()
                    .build();

            System.out.println("Fetching statistics range:");
            System.out.println(url);

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println("Response: " + response.body());

            if (response.statusCode() != 200) {
                System.err.println("Failed to fetch statistics range");
                return null;
            }

            return mapper.readValue(response.body(), models.StatisticsRangeResponse.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}