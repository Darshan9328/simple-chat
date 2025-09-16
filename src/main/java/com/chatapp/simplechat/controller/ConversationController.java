package com.chatapp.simplechat.controller;

import com.chatapp.simplechat.dto.ChatMessage;
import com.chatapp.simplechat.model.Conversation;
import com.chatapp.simplechat.model.Message;
import com.chatapp.simplechat.model.MessageStatus;
import com.chatapp.simplechat.model.User;
import com.chatapp.simplechat.repository.ConversationRepository;
import com.chatapp.simplechat.repository.MessageRepository;
import com.chatapp.simplechat.repository.UserRepository;
import com.chatapp.simplechat.service.WebSocketSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "*")
public class ConversationController {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketSessionService sessionService;

    // Get all conversations for a user
    @GetMapping("/{username}")
    public ResponseEntity<List<Map<String, Object>>> getUserConversations(@PathVariable String username) {
        List<Conversation> conversations = conversationRepository.findByUserUsername(username);

        List<Map<String, Object>> conversationDTOs = conversations.stream().map(conv -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", conv.getId());
            dto.put("otherParticipant", conv.getOtherParticipant(username));
            dto.put("lastMessage", conv.getLastMessage());
            dto.put("lastMessageTime", conv.getLastMessageTime());
            dto.put("lastMessageSender", conv.getLastMessageSender());

            // Get unread message count
            Long unreadCount = messageRepository.countUnreadMessages(conv.getId(), username);
            dto.put("unreadCount", unreadCount);

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(conversationDTOs);
    }

    // Get messages for a specific conversation
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<Message>> getConversationMessages(@PathVariable String conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return ResponseEntity.ok(messages);
    }

    // Start a new conversation or get existing one
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startConversation(@RequestBody Map<String, String> request) {
        String user1 = request.get("user1");
        String user2 = request.get("user2");

        // Check if both users exist
        Optional<User> userObj1 = userRepository.findByUsername(user1);
        Optional<User> userObj2 = userRepository.findByUsername(user2);

        if (!userObj1.isPresent() || !userObj2.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "One or both users do not exist"));
        }

        // Check if conversation already exists
        Optional<Conversation> existingConv = conversationRepository.findByTwoUsers(user1, user2);

        Conversation conversation;
        if (existingConv.isPresent()) {
            conversation = existingConv.get();
        } else {
            // Create new conversation
            conversation = new Conversation(user1, user2);
            conversation = conversationRepository.save(conversation);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("conversationId", conversation.getId());
        response.put("otherParticipant", conversation.getOtherParticipant(user1));

        return ResponseEntity.ok(response);
    }

    // Search users (exclude current user)
    @GetMapping("/search/{currentUser}")
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@PathVariable String currentUser, @RequestParam(required = false) String query) {
        List<User> users;
        System.out.println("currentUser>>"+currentUser);
        if (query == null || query.trim().isEmpty()) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByUsernameContainingIgnoreCase(query);
        }

        // Exclude current user and convert to DTO
        List<Map<String, Object>> userDTOs = users.stream()
                .filter(user -> !user.getUsername().equals(currentUser))
                .map(user -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("username", user.getUsername());
                    dto.put("id", user.getId());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    // Mark messages as read
    @PostMapping("/{conversationId}/mark-read")
    @Transactional
    public ResponseEntity<Map<String, Object>> markMessagesAsRead(
            @PathVariable String conversationId,
            @RequestParam String username) {

        // Persist READ status for all messages in conversation addressed to this username
        int updated = messageRepository.markAsRead(conversationId, username, MessageStatus.READ);

        // Broadcast READ status via WebSocket to the other participant
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        convOpt.ifPresent(conv -> {
            String other = conv.getOtherParticipant(username);
            ChatMessage status = new ChatMessage();
            status.setType(ChatMessage.MessageType.READ);
            status.setConversationId(conversationId);
            status.setSender(username); // reader
            status.setRecipient(other);
            status.setTimestamp(java.time.LocalDateTime.now().toString());

            sessionService.sendMessageToUser(other, "/queue/status", status);
            // Optionally also notify the reader for confirmation
            sessionService.sendMessageToUser(username, "/queue/status", status);
        });

        return ResponseEntity.ok(Map.of(
                "message", "Messages marked as read",
                "updated", updated
        ));
    }
}