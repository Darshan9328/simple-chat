package com.chatapp.simplechat.controller;

import com.chatapp.simplechat.dto.ChatMessage;
import com.chatapp.simplechat.model.Message;
import com.chatapp.simplechat.model.MessageStatus;
import com.chatapp.simplechat.repository.MessageRepository;
import com.chatapp.simplechat.service.ChatService;
import com.chatapp.simplechat.service.WebSocketSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class WebSocketChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private WebSocketSessionService sessionService;

    @Autowired
    private MessageRepository messageRepository;

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

            // Update chatMessage with DB-created timestamp
            if (savedMessage != null) {
                chatMessage.setTimestamp(savedMessage.getCreatedAt().toString());
            }

            // Deliver to recipient and echo to sender
            sessionService.sendMessageToUser(
                chatMessage.getRecipient(),
                "/queue/messages",
                chatMessage
            );
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
            // Persist: mark all messages in this conversation addressed to the reader as READ
            if (chatMessage.getConversationId() != null && chatMessage.getSender() != null) {
                // chatMessage.sender here is the READER username from the frontend
                String reader = chatMessage.getSender();
                List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(chatMessage.getConversationId());
                for (Message m : messages) {
                    if (reader.equals(m.getRecipientUsername()) && m.getStatus() != MessageStatus.READ) {
                        m.setStatus(MessageStatus.READ);
                    }
                }
                messageRepository.saveAll(messages);
            }

            // Notify the other participant via status queue as READ
            ChatMessage status = new ChatMessage();
            status.setType(ChatMessage.MessageType.READ);
            status.setConversationId(chatMessage.getConversationId());
            status.setSender(chatMessage.getSender()); // reader
            status.setRecipient(chatMessage.getRecipient()); // optional
            status.setTimestamp(java.time.LocalDateTime.now().toString());

            // Send to both as a status update (front-end listens on /user/queue/status)
            if (chatMessage.getRecipient() != null) {
                sessionService.sendMessageToUser(
                    chatMessage.getRecipient(),
                    "/queue/status",
                    status
                );
            }
            if (chatMessage.getSender() != null) {
                sessionService.sendMessageToUser(
                    chatMessage.getSender(),
                    "/queue/status",
                    status
                );
            }
        } catch (Exception e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
            e.printStackTrace();
        }
    }
}