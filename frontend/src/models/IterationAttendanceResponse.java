package models;

import java.util.List;

public class IterationAttendanceResponse {

    public int iterationID;
    public List<Session> sessions;
    public List<ClientAttendance> clients;

    public static class Session {
        public int sessionID;
        public String date;
    }

    public static class ClientAttendance {
        public int clientID;
        public String name;
        public List<AttendanceRecord> attendance;
    }

    public static class AttendanceRecord {
        public int sessionID;
        public boolean attended;
    }
}