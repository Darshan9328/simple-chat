package com.chatapp.simplechat.service;

import com.chatapp.simplechat.model.Message;
import com.chatapp.simplechat.model.User;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public User registerUser(String username, String password) {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
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

    public Message sendMessage(String content, String senderUsername) {
        Message message = new Message(content, senderUsername);
        return messageRepository.save(message);
    }

    public List<Message> getRecentMessages() {
        return messageRepository.findTop20ByOrderByCreatedAtDesc();
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}