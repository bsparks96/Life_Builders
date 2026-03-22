package models;

public class Client {
    private int clientID;
    private String clientFirstName;
    private String clientMiddleInitial;
    private String clientLastName;
    
    public int getClientID() {
        return clientID;
    }

    public String getClientFirstName() {
        return clientFirstName;
    }

    public String getClientMiddleInitial() {
        return clientMiddleInitial;
    }

    public String getClientLastName() {
        return clientLastName;
    }

    // --- Setters ---
    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public void setClientFirstName(String clientFirstName) {
        this.clientFirstName = clientFirstName;
    }

    public void setClientMiddleInitial(String clientMiddleInitial) {
        this.clientMiddleInitial = clientMiddleInitial;
    }

    public void setClientLastName(String clientLastName) {
        this.clientLastName = clientLastName;
    }

    public String getFullName() {
        return clientFirstName +
               (clientMiddleInitial != null && !clientMiddleInitial.isEmpty() ? " " + clientMiddleInitial + "." : "") +
               " " + clientLastName;
    }
}