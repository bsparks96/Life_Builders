package utils;

import models.ClientDetailsResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientDetailsCache {

    private static final Map<Integer, ClientDetailsResponse> clientDetailsMap = new HashMap<>();


    public static void setAllClientDetails(List<ClientDetailsResponse> clients) {
        clientDetailsMap.clear();

        for (ClientDetailsResponse client : clients) {
            clientDetailsMap.put(client.getClientID(), client);
        }
    }


    public static ClientDetailsResponse getClient(int clientID) {
        return clientDetailsMap.get(clientID);
    }


    public static boolean isLoaded() {
        return !clientDetailsMap.isEmpty();
    }
    
    public static void updateClient(ClientDetailsResponse client) {
        clientDetailsMap.put(client.getClientID(), client);
    }


    public static void clear() {
        clientDetailsMap.clear();
    }
    
    public static Map<Integer, ClientDetailsResponse> getAllClients() {
        return clientDetailsMap;
    }
}