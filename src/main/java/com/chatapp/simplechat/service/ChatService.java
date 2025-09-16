package com.chatapp.simplechat.service;

import com.chatapp.simplechat.model.Conversation;
import com.chatapp.simplechat.model.Message;
import com.chatapp.simplechat.model.User;
import com.chatapp.simplechat.repository.ConversationRepository;
import com.chatapp.simplechat.repository.MessageRepository;
import com.chatapp.simplechat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public User registerUser(String username, String password) {
    // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // Hash the password

        return userRepository.save(user);
    }

    public Authentication authenticateUser(String username, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
    }

    public Message sendPrivateMessage(String content, String senderUsername, String recipientUsername) {
        String conversationId = Message.generateConversationId(senderUsername, recipientUsername);
        
        // Create or update conversation
        Optional<Conversation> existingConv = conversationRepository.findByTwoUsers(senderUsername, recipientUsername);
        Conversation conversation;
        
        if (existingConv.isPresent()) {
            conversation = existingConv.get();
        } else {
            conversation = new Conversation(senderUsername, recipientUsername);
        }
        
        conversation.updateLastMessage(content, senderUsername);
        conversationRepository.save(conversation);
        
        // Save message
        Message message = new Message(content, senderUsername, recipientUsername, conversationId);
        return messageRepository.save(message);
    }

    public List<Message> getConversationMessages(String conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public List<Conversation> getUserConversations(String username) {
        return conversationRepository.findByUserUsername(username);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Message sendMessage(String content, String senderUsername) {
        // Implement your logic to create and save a message
        Message message = new Message();
        message.setContent(content);
        message.setSenderUsername(senderUsername);
        // Save message to database if needed
        messageRepository.save(message);
        return message;
    }
    
    public List<Message> getRecentMessages() {
        return messageRepository.findAll();
    }

        // Utility method to encode all user passwords with BCrypt
        public void encodeAllUserPasswords() {
            List<User> users = userRepository.findAll();
            for (User user : users) {
                String password = user.getPassword();
                // Only encode if not already encoded (BCrypt hashes start with $2a, $2b, or $2y)
                if (password != null && !password.startsWith("$2a") && !password.startsWith("$2b") && !password.startsWith("$2y")) {
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                }
            }
        }
}