package com.chatapp.simplechat.controller;

import com.chatapp.simplechat.dto.ChatMessage;
import com.chatapp.simplechat.model.Message;
import com.chatapp.simplechat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketChatController {

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        // Save message to database
        try {
            chatService.sendMessage(chatMessage.getContent(), chatMessage.getSender());
        } catch (Exception e) {
            System.err.println("Error saving message: " + e.getMessage());
        }

        // Return message to broadcast to all connected clients
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        // Add username to WebSocket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

        // Create join message
        ChatMessage joinMessage = new ChatMessage();
        joinMessage.setContent(chatMessage.getSender() + " joined the chat!");
        joinMessage.setSender("System");
        joinMessage.setType(ChatMessage.MessageType.JOIN);
        joinMessage.setTimestamp(java.time.LocalDateTime.now().toString());

        return joinMessage;
    }
}