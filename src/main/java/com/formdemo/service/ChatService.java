package com.formdemo.service;

import com.formdemo.exception.OpenAIException;
import com.formdemo.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final LocalIntentService localIntentService;
    private final FormGeneratorService formGeneratorService;
    
    // 存储每个会话的对话历史和表单状态
    private final Map<String, List<String>> conversationHistory = new ConcurrentHashMap<>();
    private final Map<String, List<FormField>> sessionFormFields = new ConcurrentHashMap<>(); // 当前表单字段
    private final Map<String, Map<String, Object>> sessionFormData = new ConcurrentHashMap<>(); // 当前表单数据
    private final Map<String, String> sessionFormId = new ConcurrentHashMap<>(); // 当前表单ID

    public ChatService(LocalIntentService localIntentService, FormGeneratorService formGeneratorService) {
        this.localIntentService = localIntentService;
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
        
        ChatResponse response = new ChatResponse();
        
        try {
            // 获取当前表单字段
            List<FormField> currentFormFields = sessionFormFields.getOrDefault(sessionId, new ArrayList<>());
            
            // 使用LLM识别意图
            LLMIntentResponse llmIntent = localIntentService.recognizeIntentWithLLM(message, currentFormFields);
            
            String intent = llmIntent.getIntent();
            
            // 验证意图：如果LLM识别为create_form，但用户消息不包含明确的业务意图，则强制改为chat
            if ("create_form".equals(intent) && !isValidBusinessIntent(message)) {
                intent = "chat";
                llmIntent.setIntent("chat");
                llmIntent.setFormFields(new ArrayList<>());
            }
            
            // 根据意图类型处理
            if ("create_form".equals(intent)) {
                // 创建或更新表单
                List<FormField> newFormFields = llmIntent.getFormFields();
                if (newFormFields != null && !newFormFields.isEmpty()) {
                    // 保存表单字段
                    sessionFormFields.put(sessionId, new ArrayList<>(newFormFields));
                    sessionFormData.put(sessionId, new HashMap<>());
                    
                    // 生成新的表单ID（创建新表单时）
                    String formId = UUID.randomUUID().toString();
                    sessionFormId.put(sessionId, formId);
                    
                    // 生成表单HTML
                    String formHtml = formGeneratorService.generateFormHtmlFromFields(newFormFields, sessionFormData.get(sessionId));
                    
                    response.setResponseText("好的，我已经为您创建了表单，请填写以下信息：");
                    response.setFormHtml(formHtml);
                    response.setHasForm(true);
                    response.setFormId(formId);
                    response.setIntentType("create_form");
                    response.setNeedsClarification(false);
                } else {
                    response.setResponseText("抱歉，我无法创建表单，请提供更详细的信息。");
                    response.setHasForm(false);
                    response.setIntentType("create_form");
                    response.setNeedsClarification(true);
                }
                
            } else if ("fill_form".equals(intent)) {
                // 填写表单
                Map<String, Object> fieldUpdates = llmIntent.getFieldUpdates();
                if (fieldUpdates != null && !fieldUpdates.isEmpty() && !currentFormFields.isEmpty()) {
                    // 更新表单数据
                    Map<String, Object> formData = sessionFormData.getOrDefault(sessionId, new HashMap<>());
                    formData.putAll(fieldUpdates);
                    sessionFormData.put(sessionId, formData);
                    
                    // 更新表单字段的默认值
                    updateFormFieldsWithData(currentFormFields, fieldUpdates);
                    
                    // 获取或创建表单ID（填写表单时保留现有ID）
                    String formId = sessionFormId.getOrDefault(sessionId, UUID.randomUUID().toString());
                    sessionFormId.put(sessionId, formId);
                    
                    // 重新生成表单HTML（带更新后的数据）
                    String formHtml = formGeneratorService.generateFormHtmlFromFields(currentFormFields, formData);
                    
                    response.setResponseText("好的，我已经更新了表单数据。");
                    response.setFormHtml(formHtml);
                    response.setHasForm(true);
                    response.setFormId(formId);
                    response.setIntentType("fill_form");
                    response.setNeedsClarification(false);
                } else if (currentFormFields.isEmpty()) {
                    response.setResponseText("抱歉，当前没有可填写的表单。请先创建一个表单。");
                    response.setHasForm(false);
                    response.setIntentType("fill_form");
                    response.setNeedsClarification(true);
                } else {
                    response.setResponseText("抱歉，我没有理解您要填写哪些字段。请告诉我具体要填写什么内容。");
                    response.setHasForm(true);
                    response.setIntentType("fill_form");
                    response.setNeedsClarification(true);
                }
                
            } else {
                // chat - 闲聊，不需要表单
                response.setResponseText("我理解您的意思。我可以帮您创建以下类型的表单：\n" +
                    "• 订酒店\n" +
                    "• 定机票\n" +
                    "• 请假\n" +
                    "• 报销发票\n\n" +
                    "如果您需要创建表单，请告诉我您的需求。");
                response.setHasForm(false);
                response.setIntentType("chat");
                response.setNeedsClarification(false);
            }
            
        } catch (OpenAIException e) {
            log.error("OpenAI API error in ChatService: {}", e.getMessage(), e);
            // 处理配额不足错误
            if (e.isQuotaExceeded()) {
                response.setResponseText("抱歉，OpenAI API 配额已用完，无法继续使用 AI 功能。\n\n" +
                    "解决方案：\n" +
                    "1. 请检查您的 OpenAI 账户配额和账单信息\n" +
                    "2. 访问 OpenAI 平台查看配额详情：https://platform.openai.com/account/billing\n" +
                    "3. 如果配额已用完，请充值或升级您的账户\n\n" +
                    "系统将暂时无法识别您的意图，但您仍可以使用其他功能。");
            } else {
                String errorMsg = e.getMessage() != null ? e.getMessage() : "未知错误";
                
                // 如果是连接错误，完整显示诊断信息（这些信息对用户很有用）
                if (errorMsg.contains("无法连接到") || errorMsg.contains("Failed to connect") || 
                    errorMsg.contains("Connection refused") || errorMsg.contains("❌")) {
                    // 连接错误的诊断信息应该完整显示
                    response.setResponseText(errorMsg);
                } else {
                    // 其他错误简化显示
                    if (errorMsg.contains("network") || errorMsg.contains("网络")) {
                        errorMsg = "网络连接错误，请检查您的网络连接";
                    } else if (errorMsg.length() > 200) {
                        errorMsg = errorMsg.substring(0, 200) + "...";
                    }
                    response.setResponseText("抱歉，调用 AI 服务时发生错误：\n\n" + errorMsg);
                }
            }
            response.setHasForm(false);
            response.setIntentType("chat");
            response.setNeedsClarification(true);
        } catch (Exception e) {
            log.error("Unexpected error processing message", e);
            response.setResponseText("抱歉，处理您的请求时发生错误，请稍后再试。");
            response.setHasForm(false);
            response.setIntentType("chat");
            response.setNeedsClarification(true);
        }
        
        history.add(response.getResponseText());
        return response;
    }

    /**
     * 验证是否为有效的业务意图
     * 只支持：订酒店、定机票、请假、报销发票
     */
    private boolean isValidBusinessIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        String normalizedMessage = message.trim();
        String lowerMessage = normalizedMessage.toLowerCase();
        
        // 支持的业务意图关键词
        String[] hotelKeywords = {"订酒店", "预订酒店", "酒店预订", "定酒店", "订房", "预订房间", "hotel", "book hotel"};
        String[] flightKeywords = {"定机票", "订机票", "预订机票", "机票预订", "买机票", "购票", "flight", "book flight", "book ticket", "airline"};
        String[] leaveKeywords = {"请假", "申请请假", "请假申请", "请年假", "请病假", "申请休假", "leave", "apply leave", "vacation"};
        String[] expenseKeywords = {"报销", "报销发票", "发票报销", "报销申请", "申请报销", "费用报销", "expense", "reimbursement"};
        
        // 检查是否包含业务意图关键词
        for (String keyword : hotelKeywords) {
            if (normalizedMessage.contains(keyword) || lowerMessage.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        for (String keyword : flightKeywords) {
            if (normalizedMessage.contains(keyword) || lowerMessage.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        for (String keyword : leaveKeywords) {
            if (normalizedMessage.contains(keyword) || lowerMessage.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        for (String keyword : expenseKeywords) {
            if (normalizedMessage.contains(keyword) || lowerMessage.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 更新表单字段的默认值
     */
    private void updateFormFieldsWithData(List<FormField> fields, Map<String, Object> updates) {
        for (FormField field : fields) {
            if (updates.containsKey(field.getName())) {
                Object value = updates.get(field.getName());
                field.setDefaultValue(value != null ? value.toString() : "");
            }
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
        sessionFormFields.remove(sessionId);
        sessionFormData.remove(sessionId);
        sessionFormId.remove(sessionId);
    }
}

