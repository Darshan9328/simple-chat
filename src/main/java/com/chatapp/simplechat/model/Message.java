package com.chatapp.simplechat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(name = "sender_username", nullable = false)
    private String senderUsername;

    @Column(name = "recipient_username", nullable = false)
    private String recipientUsername;

    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_status")
    private MessageStatus status = MessageStatus.SENT;

    // Constructors
    public Message() {
        this.createdAt = LocalDateTime.now();
        this.status = MessageStatus.SENT;
    }

    public Message(String content, String senderUsername, String recipientUsername, String conversationId) {
        this.content = content;
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.conversationId = conversationId;
        this.createdAt = LocalDateTime.now();
        this.status = MessageStatus.SENT;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    // Helper method to generate conversation ID
    public static String generateConversationId(String user1, String user2) {
        // Always put usernames in alphabetical order to ensure same conversation ID
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}
