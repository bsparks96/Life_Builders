package utils;

import models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDetailsCache {

    private static final Map<Integer, UserDetails> userDetailsMap = new HashMap<>();
    private static boolean loaded = false;

    public static void setUserDetails(List<UserDetails> users) {
        userDetailsMap.clear();

        for (UserDetails user : users) {
            userDetailsMap.put(user.getUserID(), user);
        }

        loaded = true;
    }

    public static UserDetails getUserByID(int userID) {
        return userDetailsMap.get(userID);
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void clear() {
        userDetailsMap.clear();
        loaded = false;
    }
    
    public static void addUser(UserDetails user) {
        userDetailsMap.put(user.getUserID(), user);
    }
}