package models;

import java.util.List;

public class IterationOut {
    private String startDate;
    private String endDate;
    private List<String> sessions;
    private int iterationID;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public List<String> getSessions() {
        return sessions;
    }

    public void setSessions(List<String> sessions) {
        this.sessions = sessions;
    }
    
    public int getIterationID() {
        return iterationID;
    }

    public void setIterationID(int iterationID) {
        this.iterationID = iterationID;
    }
}