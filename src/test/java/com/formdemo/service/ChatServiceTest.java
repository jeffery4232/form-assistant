package com.formdemo.service;

import com.formdemo.model.ChatResponse;
import com.formdemo.service.LocalIntentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private LocalIntentService localIntentService;

    @Mock
    private UserInfoService userInfoService;

    @Mock
    private FormGeneratorService formGeneratorService;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(localIntentService, userInfoService, formGeneratorService);
    }

    @Test
    void testProcessMessage_WithClarificationNeeded() {
        String sessionId = "test-session";
        String message = "我叫jeffery，打算明天去北京";
        
        com.formdemo.model.Intent intent = new com.formdemo.model.Intent();
        intent.setNeedsClarification(true);
        intent.setClarificationQuestion("请问您是想要预订酒店、购买机票，还是预订火车票呢？");
        
        when(localIntentService.recognizeIntent(anyString(), anyList())).thenReturn(intent);
        when(userInfoService.extractName(anyString())).thenReturn("jeffery");
        
        ChatResponse response = chatService.processMessage(message, sessionId);
        
        assertNotNull(response);
        assertTrue(response.isNeedsClarification());
        assertFalse(response.isHasForm());
        assertNotNull(response.getResponseText());
    }

    @Test
    void testProcessMessage_WithClearIntent() {
        String sessionId = "test-session";
        String message = "我要订酒店";
        
        com.formdemo.model.Intent intent = new com.formdemo.model.Intent();
        intent.setNeedsClarification(false);
        intent.setType("HOTEL");
        intent.setExtractedName("jeffery");
        intent.setExtractedDestination("北京");
        
        com.formdemo.model.UserInfo userInfo = new com.formdemo.model.UserInfo();
        userInfo.setName("jeffery");
        userInfo.setPhone("138****8888");
        userInfo.setEmail("test@example.com");
        userInfo.setPreferredTransportation(java.util.Arrays.asList("飞机", "高铁"));
        
        when(localIntentService.recognizeIntent(anyString(), anyList())).thenReturn(intent);
        when(userInfoService.extractName(anyString())).thenReturn("jeffery");
        when(userInfoService.getUserInfo(anyString())).thenReturn(userInfo);
        when(formGeneratorService.generateFormHtml(anyString(), any(), any())).thenReturn("<form>Test Form</form>");
        
        ChatResponse response = chatService.processMessage(message, sessionId);
        
        assertNotNull(response);
        assertFalse(response.isNeedsClarification());
        assertTrue(response.isHasForm());
        assertNotNull(response.getFormHtml());
        assertEquals("HOTEL", response.getIntentType());
    }

    @Test
    void testGetConversationHistory() {
        String sessionId = "test-session";
        chatService.processMessage("Hello", sessionId);
        
        java.util.List<String> history = chatService.getConversationHistory(sessionId);
        
        assertNotNull(history);
        assertFalse(history.isEmpty());
    }

    @Test
    void testClearSession() {
        String sessionId = "test-session";
        chatService.processMessage("Hello", sessionId);
        
        chatService.clearSession(sessionId);
        
        java.util.List<String> history = chatService.getConversationHistory(sessionId);
        assertTrue(history.isEmpty());
    }
}

