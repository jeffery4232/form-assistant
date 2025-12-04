package com.formdemo.service;

import com.formdemo.model.ChatResponse;
import com.formdemo.model.Intent;
import com.formdemo.model.UserInfo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
public class ChatService {

    private final LocalIntentService localIntentService;
    private final UserInfoService userInfoService;
    private final FormGeneratorService formGeneratorService;
    
    // 存储每个会话的对话历史和意图
    private final Map<String, List<String>> conversationHistory = new ConcurrentHashMap<>();
    private final Map<String, String> sessionIntent = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUserName = new ConcurrentHashMap<>();

    public ChatService(LocalIntentService localIntentService, UserInfoService userInfoService, 
                      FormGeneratorService formGeneratorService) {
        this.localIntentService = localIntentService;
        this.userInfoService = userInfoService;
        this.formGeneratorService = formGeneratorService;
    }

    /**
     * 处理用户消息
     */
    public ChatResponse processMessage(String message, String sessionId) {
        // 初始化会话历史
        if (!conversationHistory.containsKey(sessionId)) {
            conversationHistory.put(sessionId, new ArrayList<>());
        }
        
        List<String> history = conversationHistory.get(sessionId);
        history.add(message);
        
        // 识别意图（意图识别中会提取姓名）
        Intent intent = localIntentService.recognizeIntent(message, history);
        
        // 从意图中提取姓名，如果意图中没有，再从消息中提取
        String extractedName = intent.getExtractedName();
        if (extractedName == null || extractedName.isEmpty()) {
            extractedName = userInfoService.extractName(message);
        }
        if (extractedName != null && !extractedName.isEmpty()) {
            sessionUserName.put(sessionId, extractedName);
            // 确保意图中也包含提取到的姓名
            if (intent.getExtractedName() == null || intent.getExtractedName().isEmpty()) {
                intent.setExtractedName(extractedName);
            }
        }
        
        ChatResponse response = new ChatResponse();
        response.setNeedsClarification(intent.isNeedsClarification());
        response.setIntentType(intent.getType());
        
        // 只有当意图明确且不需要澄清时，才生成表单
        if (intent.isNeedsClarification() || intent.getType() == null || intent.getType().isEmpty()) {
            // 需要进一步澄清
            response.setResponseText(intent.getClarificationQuestion());
            response.setHasForm(false);
            history.add(intent.getClarificationQuestion());
        } else {
            // 意图明确，生成表单
            String intentType = intent.getType();
            sessionIntent.put(sessionId, intentType);
            
            // 优先使用从当前消息中提取的姓名，如果没有则使用会话中保存的姓名
            String userName = intent.getExtractedName();
            if (userName == null || userName.isEmpty()) {
                userName = sessionUserName.getOrDefault(sessionId, null);
            } else {
                // 如果从当前消息中提取到了新姓名，更新会话中的姓名
                sessionUserName.put(sessionId, userName);
            }
            
            // 获取用户信息
            UserInfo userInfo = userInfoService.getUserInfo(userName);
            
            // 生成表单
            String formHtml = formGeneratorService.generateFormHtml(intentType, intent, userInfo);
            String formId = extractFormId(formHtml);
            if (formId == null || formId.isEmpty()) {
                formId = UUID.randomUUID().toString();
            }
            
            response.setResponseText("好的，我已经为您准备好了" + getIntentTypeName(intentType) + "预订表单，请填写以下信息：");
            response.setFormHtml(formHtml);
            response.setHasForm(true);
            response.setFormId(formId);
            
            history.add(response.getResponseText());
        }
        
        return response;
    }

    private String extractFormId(String formHtml) {
        // 从HTML中提取form-id
        int start = formHtml.indexOf("data-form-id=\"");
        if (start != -1) {
            start += "data-form-id=\"".length();
            int end = formHtml.indexOf("\"", start);
            if (end != -1) {
                return formHtml.substring(start, end);
            }
        }
        return UUID.randomUUID().toString();
    }

    private String getIntentTypeName(String intentType) {
        switch (intentType) {
            case "HOTEL":
                return "酒店";
            case "FLIGHT":
                return "机票";
            case "TRAIN":
                return "火车票";
            default:
                return "预订";
        }
    }

    /**
     * 获取会话历史
     */
    public List<String> getConversationHistory(String sessionId) {
        return conversationHistory.getOrDefault(sessionId, new ArrayList<>());
    }

    /**
     * 清除会话
     */
    public void clearSession(String sessionId) {
        conversationHistory.remove(sessionId);
        sessionIntent.remove(sessionId);
        sessionUserName.remove(sessionId);
    }
}

