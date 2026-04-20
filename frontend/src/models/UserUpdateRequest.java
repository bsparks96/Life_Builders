package models;

public class UserUpdateRequest {

    private int userID;          // REQUIRED
    private String username;     // optional
    private String firstName;    // optional
    private String lastName;     // optional
    private String userRole;     // optional
    private Integer newUserID;   // optional (use Integer, not int)

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Integer getNewUserID() {
        return newUserID;
    }

    public void setNewUserID(Integer newUserID) {
        this.newUserID = newUserID;
    }
}