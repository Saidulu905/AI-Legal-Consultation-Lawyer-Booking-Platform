package com.legal.platform.controller;

import com.legal.platform.dto.AIChatRequest;
import com.legal.platform.dto.AIChatResponse;
import com.legal.platform.model.AIChatHistory;
import com.legal.platform.model.User;
import com.legal.platform.repository.UserRepository;
import com.legal.platform.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final UserRepository userRepository;

    @PostMapping("/chat")
    public ResponseEntity<AIChatResponse> chat(@RequestBody AIChatRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(aiService.processChat(user.getId(), request.getMessage()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AIChatHistory>> getHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(aiService.getChatHistory(user.getId()));
    }
}
