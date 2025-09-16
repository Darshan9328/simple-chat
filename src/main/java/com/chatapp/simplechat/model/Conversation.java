package com.chatapp.simplechat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    private String id; // user1_user2 format

    @Column(name = "user1_username", nullable = false)
    private String user1Username;

    @Column(name = "user2_username", nullable = false)
    private String user2Username;

    @Column(name = "last_message")
    private String lastMessage;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @Column(name = "last_message_sender")
    private String lastMessageSender;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Conversation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Conversation(String user1Username, String user2Username) {
        this.id = Message.generateConversationId(user1Username, user2Username);
        this.user1Username = user1Username.compareTo(user2Username) < 0 ? user1Username : user2Username;
        this.user2Username = user1Username.compareTo(user2Username) < 0 ? user2Username : user1Username;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser1Username() {
        return user1Username;
    }

    public void setUser1Username(String user1Username) {
        this.user1Username = user1Username;
    }

    public String getUser2Username() {
        return user2Username;
    }

    public void setUser2Username(String user2Username) {
        this.user2Username = user2Username;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getLastMessageSender() {
        return lastMessageSender;
    }

    public void setLastMessageSender(String lastMessageSender) {
        this.lastMessageSender = lastMessageSender;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper method to get the other participant in the conversation
    public String getOtherParticipant(String currentUser) {
        return user1Username.equals(currentUser) ? user2Username : user1Username;
    }

    // Update conversation with new message
    public void updateLastMessage(String message, String sender) {
        this.lastMessage = message;
        this.lastMessageSender = sender;
        this.lastMessageTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}