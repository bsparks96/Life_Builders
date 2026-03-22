package utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class CourseCache {

    // Key: courseID, Value: courseName
    public static Map<Integer, String> courseMap = new HashMap<>();

    // For looking up ID by name (reverse lookup)
    private static final Map<String, Integer> courseNameToIdMap = new HashMap<>();

    // List of all course names for UI dropdowns
    private static final ObservableList<String> courseNames = FXCollections.observableArrayList();

    // Called after API fetch
    public static void setAvailableCourses(Map<String, Integer> nameToIdMap) {
        courseNameToIdMap.clear();
        courseNameToIdMap.putAll(nameToIdMap);

        courseMap.clear();
        for (Map.Entry<String, Integer> entry : nameToIdMap.entrySet()) {
            courseMap.put(entry.getValue(), entry.getKey());
        }

        courseNames.clear();
        courseNames.addAll(nameToIdMap.keySet());
    }

    public static ObservableList<String> getCourseNames() {
        return courseNames;
    }

    public static Map<String, Integer> getCourseNameToIdMap() {
        return courseNameToIdMap;
    }
    
    public static Integer getCourseID(String courseName) {
        return courseNameToIdMap.get(courseName);
    }
}