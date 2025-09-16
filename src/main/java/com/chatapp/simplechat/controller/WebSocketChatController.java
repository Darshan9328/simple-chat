package com.chatapp.simplechat.controller;

import com.chatapp.simplechat.dto.ChatMessage;
import com.chatapp.simplechat.model.Message;
import com.chatapp.simplechat.service.ChatService;
import com.chatapp.simplechat.service.WebSocketSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketChatController {

    @Autowired
    private ChatService chatService;


    @Autowired
    private WebSocketSessionService sessionService;

    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        try {
            // Generate conversation ID if not provided
            if (chatMessage.getConversationId() == null || chatMessage.getConversationId().isEmpty()) {
                String conversationId = Message.generateConversationId(
                    chatMessage.getSender(), 
                    chatMessage.getRecipient()
                );
                chatMessage.setConversationId(conversationId);
            }
            
            // Set message type and timestamp
            chatMessage.setType(ChatMessage.MessageType.CHAT);
            chatMessage.setTimestamp(java.time.LocalDateTime.now().toString());
            chatMessage.setStatus("SENT");
            
            // Save message to database
            Message savedMessage = chatService.sendPrivateMessage(
                chatMessage.getContent(), 
                chatMessage.getSender(), 
                chatMessage.getRecipient()
            );
            
            // Update chatMessage with database ID if needed
            if (savedMessage != null) {
                chatMessage.setTimestamp(savedMessage.getCreatedAt().toString());
            }

            // Send message to recipient (if they are online)
            sessionService.sendMessageToUser(
                chatMessage.getRecipient(),
                "/queue/messages",
                chatMessage
            );

            // Send confirmation back to sender (so they can see their own message)
            sessionService.sendMessageToUser(
                chatMessage.getSender(),
                "/queue/messages",
                chatMessage
            );

            System.out.println("=== WebSocket Message Sent ===");
            System.out.println("From: " + chatMessage.getSender());
            System.out.println("To: " + chatMessage.getRecipient());
            System.out.println("Content: " + chatMessage.getContent());
            System.out.println("ConversationId: " + chatMessage.getConversationId());
            System.out.println("Timestamp: " + chatMessage.getTimestamp());
            System.out.println("===============================");

        } catch (Exception e) {
            System.err.println("Error sending private message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        // Add username to WebSocket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        
        // Register user in session service
        sessionService.addUserSession(chatMessage.getSender(), headerAccessor.getSessionId());
        
        // Notify user they are connected
        sessionService.sendMessageToUser(
            chatMessage.getSender(),
            "/queue/status",
            "Connected successfully"
        );
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload ChatMessage chatMessage) {
        // Send typing notification to recipient
        if (chatMessage.getRecipient() != null) {
            sessionService.sendMessageToUser(
                chatMessage.getRecipient(),
                "/queue/typing",
                chatMessage
            );
        }
    }

    @MessageMapping("/chat.markAsRead")
    public void markAsRead(@Payload ChatMessage chatMessage) {
        try {
            // Here you would update message status in the database
            // For now, just notify the sender that messages were read
            if (chatMessage.getSender() != null) {
                sessionService.sendMessageToUser(
                    chatMessage.getSender(),
                    "/queue/read-receipt",
                    chatMessage
                );
            }
        } catch (Exception e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
        }
    }
}