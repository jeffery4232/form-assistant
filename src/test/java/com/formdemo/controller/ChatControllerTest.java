package com.formdemo.controller;

import com.formdemo.model.ChatMessage;
import com.formdemo.model.ChatResponse;
import com.formdemo.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    private ChatController chatController;

    @BeforeEach
    void setUp() {
        chatController = new ChatController(chatService);
    }

    @Test
    void testSendMessage() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage("我要订酒店");
        chatMessage.setSessionId("test-session");
        
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setResponseText("好的，我已经为您准备好了酒店预订表单");
        chatResponse.setHasForm(true);
        chatResponse.setFormHtml("<form>Test</form>");
        
        when(chatService.processMessage(anyString(), anyString())).thenReturn(chatResponse);
        
        ResponseEntity<ChatResponse> response = chatController.sendMessage(chatMessage);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(chatService, times(1)).processMessage(anyString(), anyString());
    }

    @Test
    void testSubmitForm() {
        Map<String, Object> formData = new HashMap<>();
        formData.put("name", "jeffery");
        formData.put("destination", "北京");
        
        ResponseEntity<Map<String, Object>> response = chatController.submitForm(formData);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    void testClearSession() {
        String sessionId = "test-session";
        
        ResponseEntity<Map<String, String>> response = chatController.clearSession(sessionId);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(chatService, times(1)).clearSession(sessionId);
    }
}

