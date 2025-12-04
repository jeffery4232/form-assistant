package com.formdemo.controller;

import com.formdemo.model.ChatMessage;
import com.formdemo.model.ChatResponse;
import com.formdemo.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatMessage chatMessage) {
        String sessionId = chatMessage.getSessionId() != null ? chatMessage.getSessionId() : "default";
        ChatResponse response = chatService.processMessage(chatMessage.getMessage(), sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/form/submit")
    public ResponseEntity<Map<String, Object>> submitForm(@RequestBody Map<String, Object> formData) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "表单提交成功！");
        response.put("data", formData);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, String>> clearSession(@PathVariable String sessionId) {
        chatService.clearSession(sessionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "会话已清除");
        return ResponseEntity.ok(response);
    }
}

