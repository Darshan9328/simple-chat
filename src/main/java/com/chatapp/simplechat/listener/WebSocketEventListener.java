package com.chatapp.simplechat.listener;

import com.chatapp.simplechat.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new WebSocket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            logger.info("User disconnected: " + username);

            // Create leave message
            ChatMessage leaveMessage = new ChatMessage();
            leaveMessage.setContent(username + " left the chat!");
            leaveMessage.setSender("System");
            leaveMessage.setType(ChatMessage.MessageType.LEAVE);
            leaveMessage.setTimestamp(java.time.LocalDateTime.now().toString());

            // Broadcast leave message to all connected clients
            messagingTemplate.convertAndSend("/topic/public", leaveMessage);
        }
    }
}