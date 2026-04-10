package utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.IterationAttendanceResponse;

import java.util.*;

public class CourseSessionCache {


    private static final Map<Integer, List<SessionInfo>> iterationSessions = new HashMap<>();

    private static final Map<Integer, List<ClientAttendance>> iterationClients = new HashMap<>();

    private static final Map<String, Boolean> attendanceMap = new HashMap<>();


    public static class SessionInfo {
        public int sessionID;
        public String date;

        public SessionInfo(int sessionID, String date) {
            this.sessionID = sessionID;
            this.date = date;
        }
    }

    public static class ClientAttendance {
        public int clientID;
        public String name;
        public Map<Integer, Boolean> attendanceBySession = new HashMap<>();

        public ClientAttendance(int clientID, String name) {
            this.clientID = clientID;
            this.name = name;
        }
    }



    public static void setIterationData(
            int iterationID,
            List<SessionInfo> sessions,
            List<ClientAttendance> clients
    ) {
        iterationSessions.put(iterationID, sessions);
        iterationClients.put(iterationID, clients);

        for (ClientAttendance client : clients) {
            for (Map.Entry<Integer, Boolean> entry : client.attendanceBySession.entrySet()) {
                String key = buildKey(iterationID, client.clientID, entry.getKey());
                attendanceMap.put(key, entry.getValue());
            }
        }
    }



    public static List<SessionInfo> getSessions(int iterationID) {
        return iterationSessions.getOrDefault(iterationID, new ArrayList<>());
    }

    public static List<ClientAttendance> getClients(int iterationID) {
        return iterationClients.getOrDefault(iterationID, new ArrayList<>());
    }

    public static Boolean getAttendance(int iterationID, int clientID, int sessionID) {
        return attendanceMap.getOrDefault(
                buildKey(iterationID, clientID, sessionID),
                false
        );
    }


    public static void updateAttendance(
            int iterationID,
            int clientID,
            int sessionID,
            boolean attended
    ) {
        String key = buildKey(iterationID, clientID, sessionID);
        attendanceMap.put(key, attended);

        List<ClientAttendance> clients = iterationClients.get(iterationID);
        if (clients != null) {
            for (ClientAttendance client : clients) {
                if (client.clientID == clientID) {
                    client.attendanceBySession.put(sessionID, attended);
                    break;
                }
            }
        }
    }


    private static String buildKey(int iterationID, int clientID, int sessionID) {
        return iterationID + "-" + clientID + "-" + sessionID;
    }

    public static void clearIteration(int iterationID) {
        iterationSessions.remove(iterationID);
        iterationClients.remove(iterationID);

        attendanceMap.keySet().removeIf(key -> key.startsWith(iterationID + "-"));
    }

    public static boolean isLoaded(int iterationID) {
        return iterationSessions.containsKey(iterationID)
                && iterationClients.containsKey(iterationID);
    }
    
    
    public static void setFromResponse(IterationAttendanceResponse response) {

        List<SessionInfo> sessions = new ArrayList<>();
        for (IterationAttendanceResponse.Session s : response.sessions) {
            sessions.add(new SessionInfo(s.sessionID, s.date));
        }

        List<ClientAttendance> clients = new ArrayList<>();

        for (IterationAttendanceResponse.ClientAttendance c : response.clients) {

            ClientAttendance client = new ClientAttendance(c.clientID, c.name);

            for (IterationAttendanceResponse.AttendanceRecord a : c.attendance) {
                client.attendanceBySession.put(a.sessionID, a.attended);
            }

            clients.add(client);
        }

        setIterationData(response.iterationID, sessions, clients);
    }
}