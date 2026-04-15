package models;

import java.util.List;

public class CompletionUpdateRequest {

    public List<CompletionRecord> updates;

    public static class CompletionRecord {
        public int clientID;
        public int courseID;
        public int iterationID;
        public String completionDate; 
    }
}