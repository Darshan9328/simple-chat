package com.chatapp.simplechat.dto;

public class ChatMessage {
    private String content;
    private String sender;
    private String recipient;
    private String conversationId;
    private MessageType type;
    private String timestamp;
    private String status;

    public enum MessageType {
        CHAT, // Regular chat message
        JOIN, // User joined the chat
        LEAVE, // User left the chat
        TYPING, // User is typing
        ONLINE, // User came online
        OFFLINE, // User went offline
        READ // Read receipt / status update
    }

    // Default constructor
    public ChatMessage() {
    }

    // Constructor with parameters
    public ChatMessage(String content, String sender, MessageType type) {
        this.content = content;
        this.sender = sender;
        this.type = type;
        this.timestamp = java.time.LocalDateTime.now().toString();
        this.status = "SENT";
    }

    // Constructor for private messages
    public ChatMessage(String content, String sender, String recipient, String conversationId, MessageType type) {
        this.content = content;
        this.sender = sender;
        this.recipient = recipient;
        this.conversationId = conversationId;
        this.type = type;
        this.timestamp = java.time.LocalDateTime.now().toString();
        this.status = "SENT";
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}