package utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class UserCache {

    private static final Map<String, Integer> userNameToIdMap = new HashMap<>();
    private static final ObservableList<String> userNames = FXCollections.observableArrayList();

    public static void setAvailableUsers(Map<String, Integer> nameToIdMap) {
        userNameToIdMap.clear();
        userNameToIdMap.putAll(nameToIdMap);

        userNames.clear();
        userNames.addAll(nameToIdMap.keySet());
    }

    public static ObservableList<String> getUserNames() {
        return userNames;
    }

    public static Map<String, Integer> getUserNameToIdMap() {
        return userNameToIdMap;
    }

    public static Integer getUserID(String name) {
        return userNameToIdMap.get(name);
    }
    
    
    public static String getUserNameByID(int id) {
        for (Map.Entry<String, Integer> entry : userNameToIdMap.entrySet()) {
            if (entry.getValue() == id) {
                return entry.getKey();
            }
        }
        return "Unknown Instructor (ID: " + id + ")";
    }
}
