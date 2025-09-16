package com.chatapp.simplechat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

@Service
public class WebSocketSessionService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Map to store user sessions: username -> sessionId
    private final ConcurrentHashMap<String, String> userSessions = new ConcurrentHashMap<>();
    
    // Map to store session to user mapping: sessionId -> username
    private final ConcurrentHashMap<String, String> sessionUsers = new ConcurrentHashMap<>();

    public void addUserSession(String username, String sessionId) {
        userSessions.put(username, sessionId);
        sessionUsers.put(sessionId, username);
        System.out.println("=== User Session Added ===");
        System.out.println("Username: " + username);
        System.out.println("Session ID: " + sessionId);
        System.out.println("Total active sessions: " + userSessions.size());
        System.out.println("=========================");
    }

    public void removeUserSession(String sessionId) {
        String username = sessionUsers.remove(sessionId);
        if (username != null) {
            userSessions.remove(username);
            System.out.println("=== User Session Removed ===");
            System.out.println("Username: " + username);
            System.out.println("Session ID: " + sessionId);
            System.out.println("Total active sessions: " + userSessions.size());
            System.out.println("============================");
        }
    }

    public boolean isUserOnline(String username) {
        return userSessions.containsKey(username);
    }

    public Set<String> getOnlineUsers() {
        return userSessions.keySet();
    }

    public void sendMessageToUser(String username, String destination, Object message) {
        // Try user-destination delivery (works when Principal is set)
        try {
            messagingTemplate.convertAndSendToUser(username, destination, message);
            System.out.println("Message sent to user-destination: " + username + " -> " + destination);
        } catch (Exception ex) {
            System.out.println("User-destination delivery fallback for: " + username + " -> " + destination + ", reason: " + ex.getMessage());
        }

        // Always publish a user-specific topic as a fallback (no Principal required)
        String topicDest = "/topic/user/" + username + destination;
        messagingTemplate.convertAndSend(topicDest, message);
        System.out.println("Message also broadcast to topic: " + topicDest);
    }

    public void sendMessageToAllUsers(String destination, Object message) {
        for (String username : userSessions.keySet()) {
            sendMessageToUser(username, destination, message);
        }
    }
}
