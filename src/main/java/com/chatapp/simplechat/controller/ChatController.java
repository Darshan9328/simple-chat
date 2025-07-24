package com.chatapp.simplechat.controller;

import com.chatapp.simplechat.dto.JwtRequest;
import com.chatapp.simplechat.dto.JwtResponse;
import com.chatapp.simplechat.model.Message;
import com.chatapp.simplechat.model.User;
import com.chatapp.simplechat.security.JwtUtil;
import com.chatapp.simplechat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody JwtRequest request) {
        try {
            User user = chatService.registerUser(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(Map.of("message", "User registered successfully", "userId", user.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest request) {
        try {
            Authentication authentication = chatService.authenticateUser(request.getUsername(), request.getPassword());

            if (authentication.isAuthenticated()) {
                String token = jwtUtil.generateToken(request.getUsername());
                return ResponseEntity.ok(new JwtResponse(token, request.getUsername()));
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> request) {
        try {
            // Get the authenticated user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("authentication>>>"+ authentication);
            String senderUsername = authentication.getName();

            String content = request.get("content");

            Message message = chatService.sendMessage(content, senderUsername);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getMessages() {
        List<Message> messages = chatService.getRecentMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return ResponseEntity.ok(Map.of("username", username));
    }
}