package utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.CourseDetailsResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseCache {

    public static Map<Integer, String> courseMap = new HashMap<>();

    private static final Map<String, Integer> courseNameToIdMap = new HashMap<>();
    
    private static final Map<Integer, CourseDetailsResponse> courseDetailsMap = new HashMap<>();

    private static final ObservableList<String> courseNames = FXCollections.observableArrayList();

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

    
    public static void setCourseDetails(List<CourseDetailsResponse> courses) {
        courseDetailsMap.clear();

        for (CourseDetailsResponse course : courses) {
            courseDetailsMap.put(course.getCourseID(), course);
        }
    }

    public static CourseDetailsResponse getCourseDetails(int courseID) {
        return courseDetailsMap.get(courseID);
    }

    public static boolean isDetailsLoaded() {
        return !courseDetailsMap.isEmpty();
    }
    
    public static List<CourseDetailsResponse> getAllCourseDetails() {
        return new ArrayList<>(courseDetailsMap.values());
    }
}