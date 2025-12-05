package com.formdemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdemo.exception.OpenAIException;
import com.formdemo.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocalIntentService {

    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper;
    
    /**
     * 字段类型映射表：将中英文类型名称映射到标准HTML表单字段类型
     */
    private static final Map<String, String> FIELD_TYPE_MAP = new HashMap<>();
    
    static {
        FIELD_TYPE_MAP.put("日期", "date");
        FIELD_TYPE_MAP.put("date", "date");
        FIELD_TYPE_MAP.put("时间", "datetime-local");
        FIELD_TYPE_MAP.put("datetime", "datetime-local");
        FIELD_TYPE_MAP.put("姓名", "text");
        FIELD_TYPE_MAP.put("name", "text");
        FIELD_TYPE_MAP.put("名字", "text");
        FIELD_TYPE_MAP.put("生日", "date");
        FIELD_TYPE_MAP.put("birthday", "birthday");
        FIELD_TYPE_MAP.put("性别", "select");
        FIELD_TYPE_MAP.put("gender", "select");
        FIELD_TYPE_MAP.put("sex", "select");
        FIELD_TYPE_MAP.put("邮箱", "email");
        FIELD_TYPE_MAP.put("email", "email");
        FIELD_TYPE_MAP.put("电话", "tel");
        FIELD_TYPE_MAP.put("phone", "tel");
        FIELD_TYPE_MAP.put("数字", "number");
        FIELD_TYPE_MAP.put("number", "number");
        FIELD_TYPE_MAP.put("文本", "text");
        FIELD_TYPE_MAP.put("text", "text");
        FIELD_TYPE_MAP.put("多行文本", "textarea");
        FIELD_TYPE_MAP.put("textarea", "textarea");
        FIELD_TYPE_MAP.put("密码", "password");
        FIELD_TYPE_MAP.put("password", "password");
        FIELD_TYPE_MAP.put("选择", "select");
        FIELD_TYPE_MAP.put("select", "select");
        FIELD_TYPE_MAP.put("复选框", "checkbox");
        FIELD_TYPE_MAP.put("checkbox", "checkbox");
        FIELD_TYPE_MAP.put("单选", "radio");
        FIELD_TYPE_MAP.put("radio", "radio");
    }

    public LocalIntentService(OpenAIService openAIService) {
        this.openAIService = openAIService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 使用LLM进行意图识别
     * @param userMessage 用户消息
     * @param currentFormFields 当前表单字段列表（可能为空）
     * @return LLM意图识别响应
     */
    public LLMIntentResponse recognizeIntentWithLLM(String userMessage, List<FormField> currentFormFields) {
        try {
            // 构建提示词
            String context = buildFormContext(currentFormFields);
            String prompt = buildIntentPrompt(context, userMessage);

            // 构建消息列表
            List<OpenAIRequest.Message> messages = new ArrayList<>();
            messages.add(new OpenAIRequest.Message("system", "你是一个表单构建与填写助手。只有在用户明确表达业务意图（订酒店、定机票、请假、报销发票）时才创建表单。对于自我介绍、聊天等非业务意图，必须返回 chat 意图。"));
            messages.add(new OpenAIRequest.Message("user", prompt));

            // 调用OpenAI API
            String responseContent = openAIService.callOpenAI(messages);
            
            // 清理响应内容，移除可能的markdown代码块标记
            String jsonContent = cleanJsonResponse(responseContent);
            
            // 解析为LLMIntentResponse
            LLMIntentResponse intentResponse = objectMapper.readValue(jsonContent, LLMIntentResponse.class);
            
            return intentResponse;
            
        } catch (OpenAIException e) {
            // 如果是配额不足错误，抛出特殊异常以便上层处理
            if (e.isQuotaExceeded()) {
                throw e;
            }
            // 其他 OpenAI 错误，返回默认的 chat 意图
            LLMIntentResponse fallback = new LLMIntentResponse();
            fallback.setIntent("chat");
            fallback.setFormFields(new ArrayList<>());
            fallback.setFieldUpdates(new java.util.HashMap<>());
            return fallback;
        } catch (Exception e) {
            // 返回默认的chat意图
            LLMIntentResponse fallback = new LLMIntentResponse();
            fallback.setIntent("chat");
            fallback.setFormFields(new ArrayList<>());
            fallback.setFieldUpdates(new java.util.HashMap<>());
            return fallback;
        }
    }

    /**
     * 构建表单上下文描述
     */
    private String buildFormContext(List<FormField> formFields) {
        if (formFields == null || formFields.isEmpty()) {
            return "[]";
        }
        
        try {
            // 规范化字段类型
            List<FormField> normalizedFields = new ArrayList<>();
            for (FormField field : formFields) {
                FormField normalizedField = new FormField(
                    field.getName(),
                    field.getLabel(),
                    normalizeFieldType(field.getType()),
                    field.getDefaultValue(),
                    field.getOptions(),
                    field.isRequired(),
                    field.getPlaceholder()
                );
                normalizedFields.add(normalizedField);
            }
            
            // 使用ObjectMapper将表单字段转换为JSON格式用于上下文
            return objectMapper.writeValueAsString(normalizedFields);
        } catch (Exception e) {
            return "[]";
        }
    }
    
    /**
     * 规范化字段类型：根据映射表将类型名称转换为标准HTML表单字段类型
     * @param type 原始类型名称
     * @return 标准化后的类型名称
     */
    private String normalizeFieldType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "text"; // 默认类型
        }
        
        String normalizedType = type.trim();
        
        // 先尝试直接匹配（不区分大小写）
        String mappedType = FIELD_TYPE_MAP.get(normalizedType);
        if (mappedType != null) {
            return mappedType;
        }
        
        // 尝试小写匹配
        mappedType = FIELD_TYPE_MAP.get(normalizedType.toLowerCase());
        if (mappedType != null) {
            return mappedType;
        }
        
        // 如果映射表中没有，返回原始类型（可能是已标准化的类型）
        return normalizedType;
    }

    /**
     * 构建意图识别的提示词
     */
    private String buildIntentPrompt(String context, String userMessage) {
        return "现有表单定义（可能为空）：\n\n" + context + "\n\n" +
               "用户的自然语言输入：\n\n" + userMessage + "\n\n" +
               "请返回JSON，必须包含：\n\n" +
               "- intent: \"create_form\" | \"fill_form\" | \"chat\"\n\n" +
               "- form_fields: 当需要创建或更新表单结构时的字段数组（格式同前），否则返回 []\n\n" +
               "- field_updates: 当 intent 为 fill_form 时，需要填写/修改的字段键值对，键请使用字段的 name（若只提供 label，请结合上下文推断 name）。无变更则为空对象。\n\n" +
               "重要规则：\n\n" +
               "1. 只有在用户明确表达以下业务意图时，才将 intent 设为 create_form：\n" +
               "   - 订酒店/预订酒店/酒店预订\n" +
               "   - 定机票/预订机票/机票预订\n" +
               "   - 请假/申请请假/请假申请\n" +
               "   - 报销发票/发票报销/报销申请\n\n" +
               "2. 以下情况必须将 intent 设为 chat（不创建表单）：\n" +
               "   - 自我介绍（如：\"我叫xxx\"、\"我是xxx\"）\n" +
               "   - 普通聊天、问候、闲聊\n" +
               "   - 询问时间、天气等非业务相关问题\n" +
               "   - 没有明确业务意图的对话\n\n" +
               "3. 如果用户仅提供要填写的内容（如\"把姓名填成张三\"），intent 设为 fill_form，将相应字段写入 field_updates。\n\n" +
               "4. 如果用户输入只是个人信息介绍或聊天，没有明确的业务意图，intent 必须设为 chat，form_fields 返回空数组 []。\n\n" +
               "5. 只返回 JSON，不要额外文字。";
    }

    /**
     * 清理JSON响应，移除可能的markdown代码块标记
     */
    private String cleanJsonResponse(String response) {
        String cleaned = response.trim();
        // 移除可能的markdown代码块标记
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

}

