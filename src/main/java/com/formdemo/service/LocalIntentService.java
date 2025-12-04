package com.formdemo.service;

import com.formdemo.model.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LocalIntentService {

    private static final Logger log = LoggerFactory.getLogger(LocalIntentService.class);

    private static final List<String> HOTEL_KEYWORDS = Arrays.asList(
            "酒店", "住宿", "宾馆", "旅馆", "住", "房间", "订房", "预订酒店"
    );

    private static final List<String> FLIGHT_KEYWORDS = Arrays.asList(
            "飞机", "航班", "机票", "航空", "飞行", "飞", "买机票", "订机票"
    );

    private static final List<String> TRAIN_KEYWORDS = Arrays.asList(
            "火车", "高铁", "动车", "列车", "铁路", "火车票", "订火车票"
    );

    private static final List<String> TRAVEL_KEYWORDS = Arrays.asList(
            "去", "到", "前往", "旅行", "出行", "旅游"
    );

    /**
     * 使用本地规则进行意图识别和对话
     */
    public String chatWithAI(String userMessage, List<String> conversationHistory) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "您好，请问我可以为您做些什么？";
        }

        String input = userMessage.toLowerCase().trim();
        
        // 检查是否包含旅行相关关键词
        boolean hasTravelIntent = TRAVEL_KEYWORDS.stream()
                .anyMatch(input::contains);

        // 检查酒店意图
        boolean hasHotelIntent = HOTEL_KEYWORDS.stream()
                .anyMatch(input::contains);
        
        // 检查机票意图
        boolean hasFlightIntent = FLIGHT_KEYWORDS.stream()
                .anyMatch(input::contains);
        
        // 检查火车票意图
        boolean hasTrainIntent = TRAIN_KEYWORDS.stream()
                .anyMatch(input::contains);

        // 如果同时有多个意图或没有明确意图
        int intentCount = 0;
        if (hasHotelIntent) intentCount++;
        if (hasFlightIntent) intentCount++;
        if (hasTrainIntent) intentCount++;

        // 检查历史对话中是否已经有明确的意图
        String previousIntent = extractPreviousIntent(conversationHistory);

        if (intentCount == 0 && hasTravelIntent && previousIntent == null) {
            // 有旅行意图但没有明确的服务类型
            return "我注意到您有出行计划。为了更好地为您服务，请告诉我您需要预订酒店、购买机票，还是预订火车票呢？";
        }

        if (intentCount > 1) {
            // 多个意图，需要澄清
            return "我注意到您可能同时提到了多种出行方式。为了准确为您服务，请告诉我您主要需要预订哪一项：酒店、机票，还是火车票？";
        }

        // 明确识别到单一意图
        if (hasHotelIntent && !hasFlightIntent && !hasTrainIntent) {
            return "好的，我已经为您准备好了酒店预订表单，请填写以下信息：";
        } else if (hasFlightIntent && !hasHotelIntent && !hasTrainIntent) {
            return "好的，我已经为您准备好了机票预订表单，请填写以下信息：";
        } else if (hasTrainIntent && !hasHotelIntent && !hasFlightIntent) {
            return "好的，我已经为您准备好了火车票预订表单，请填写以下信息：";
        }

        // 如果之前有意图，继续使用（但需要确保用户消息中没有冲突的意图）
        if (previousIntent != null && !previousIntent.isEmpty() && intentCount == 0) {
            return "好的，我已经为您准备好了" + getIntentTypeName(previousIntent) + "预订表单，请填写以下信息：";
        }

        // 默认需要澄清
        return "抱歉，我没有完全理解您的需求。请告诉我您需要预订酒店、购买机票，还是预订火车票？";
    }

    /**
     * 识别用户意图
     */
    public Intent recognizeIntent(String userMessage, List<String> conversationHistory) {
        String aiResponse = chatWithAI(userMessage, conversationHistory);
        
        Intent intent = new Intent();
        intent.setNeedsClarification(true);
        intent.setClarificationQuestion(aiResponse);
        
        // 从用户消息中提取意图类型（必须从用户消息中明确识别，不能仅从AI回复中推断）
        String lowerMessage = userMessage.toLowerCase();
        
        // 检查用户消息中是否明确包含意图关键词
        boolean hasHotelKeyword = lowerMessage.contains("酒店") || lowerMessage.contains("住宿") || 
                                  lowerMessage.contains("宾馆") || lowerMessage.contains("旅馆") ||
                                  lowerMessage.contains("订房");
        boolean hasFlightKeyword = lowerMessage.contains("机票") || lowerMessage.contains("飞机") || 
                                  lowerMessage.contains("航班") || lowerMessage.contains("航空");
        boolean hasTrainKeyword = lowerMessage.contains("火车") || lowerMessage.contains("高铁") || 
                                 lowerMessage.contains("动车") || lowerMessage.contains("火车票");
        
        // 只有当用户消息中明确包含意图关键词时，才设置意图类型
        if (hasHotelKeyword && !hasFlightKeyword && !hasTrainKeyword) {
            intent.setType("HOTEL");
            intent.setConfidence(0.9);
            intent.setNeedsClarification(false);
        } else if (hasFlightKeyword && !hasHotelKeyword && !hasTrainKeyword) {
            intent.setType("FLIGHT");
            intent.setConfidence(0.9);
            intent.setNeedsClarification(false);
        } else if (hasTrainKeyword && !hasHotelKeyword && !hasFlightKeyword) {
            intent.setType("TRAIN");
            intent.setConfidence(0.9);
            intent.setNeedsClarification(false);
        } else {
            // 如果没有明确的单一意图，保持需要澄清状态
            intent.setNeedsClarification(true);
            intent.setType(null);
        }
        
        // 提取姓名 - 支持多种模式
        // 模式1: 我叫/我是/姓名/名字 + 姓名
        Pattern namePattern1 = Pattern.compile("(?:我叫|我是|姓名|名字)[：:：]?\\s*([\\u4e00-\\u9fa5a-zA-Z]+)");
        Matcher nameMatcher1 = namePattern1.matcher(userMessage);
        if (nameMatcher1.find()) {
            intent.setExtractedName(nameMatcher1.group(1));
        } else {
            // 模式2: 帮...定/订/买 + 服务类型（提取"帮"后面的姓名）
            Pattern namePattern2 = Pattern.compile("帮([\\u4e00-\\u9fa5a-zA-Z]+)(?:定|订|买)");
            Matcher nameMatcher2 = namePattern2.matcher(userMessage);
            if (nameMatcher2.find()) {
                intent.setExtractedName(nameMatcher2.group(1));
            } else {
                // 模式3: 为...定/订/买 + 服务类型（提取"为"后面的姓名）
                Pattern namePattern3 = Pattern.compile("为([\\u4e00-\\u9fa5a-zA-Z]+)(?:定|订|买)");
                Matcher nameMatcher3 = namePattern3.matcher(userMessage);
                if (nameMatcher3.find()) {
                    intent.setExtractedName(nameMatcher3.group(1));
                } else {
                    // 模式4: 给...定/订/买 + 服务类型（提取"给"后面的姓名）
                    Pattern namePattern4 = Pattern.compile("给([\\u4e00-\\u9fa5a-zA-Z]+)(?:定|订|买)");
                    Matcher nameMatcher4 = namePattern4.matcher(userMessage);
                    if (nameMatcher4.find()) {
                        intent.setExtractedName(nameMatcher4.group(1));
                    }
                }
            }
        }
        
        // 提取目的地
        Pattern destPattern = Pattern.compile("(?:去|到|前往)[：:：]?\\s*([\\u4e00-\\u9fa5]+)");
        Matcher destMatcher = destPattern.matcher(userMessage);
        if (destMatcher.find()) {
            intent.setExtractedDestination(destMatcher.group(1));
        }
        
        // 提取日期
        Pattern datePattern = Pattern.compile("(?:明天|后天|今天|\\d+月\\d+日|\\d{4}-\\d{2}-\\d{2})");
        Matcher dateMatcher = datePattern.matcher(userMessage);
        if (dateMatcher.find()) {
            intent.setExtractedDate(dateMatcher.group(0));
        }
        
        return intent;
    }

    private String extractPreviousIntent(List<String> conversationHistory) {
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return null;
        }
        
        // 从历史对话中查找已明确的意图
        for (int i = conversationHistory.size() - 1; i >= 0; i--) {
            String msg = conversationHistory.get(i).toLowerCase();
            if (msg.contains("酒店")) {
                return "HOTEL";
            } else if (msg.contains("机票") || msg.contains("飞机")) {
                return "FLIGHT";
            } else if (msg.contains("火车") || msg.contains("高铁")) {
                return "TRAIN";
            }
        }
        
        return null;
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
}

