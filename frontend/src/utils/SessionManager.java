package utils;

public class SessionManager {

	
    private static String token = null;
    private static String username = null;
    private static int userID = -1;
    private static String role = null;


    public static void setToken(String newToken) {
        token = newToken;
    }

    public static String getToken() {
        return token;
    }


    public static void setUser(String uname, int id, String userRole) {
        username = uname;
        userID = id;
        role = userRole;
    }

    public static String getUsername() {
        return username;
    }

    public static int getUserID() {
        return userID;
    }

    public static String getRole() {
        return role;
    }


    public static boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    
    public static void clearSession() {
        token = null;
        username = null;
        userID = -1;
        role = null;
    }
}