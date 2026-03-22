package models;

public class InstructorOut {
    private String instructorName;
    
    private int userID;

    public int getUserID() { 
    	return userID; 
    }
    
    public void setUserID(int userID) { this.userID = userID; }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }
}