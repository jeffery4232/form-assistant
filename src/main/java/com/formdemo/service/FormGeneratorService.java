package com.formdemo.service;

import com.formdemo.model.FormField;
import com.formdemo.model.Intent;
import com.formdemo.model.UserInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FormGeneratorService {

    /**
     * 根据意图生成表单HTML
     */
    public String generateFormHtml(String intentType, Intent intent, UserInfo userInfo) {
        String formId = UUID.randomUUID().toString();
        List<FormField> fields = generateFormFields(intentType, intent, userInfo);
        
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"form-container\" data-form-id=\"").append(formId).append("\">");
        html.append("<form id=\"form-").append(formId).append("\" class=\"dynamic-form\">");
        
        for (FormField field : fields) {
            html.append(generateFieldHtml(field));
        }
        
        html.append("<div class=\"form-actions\">");
        html.append("<button type=\"submit\" class=\"btn-submit\">提交</button>");
        html.append("<button type=\"button\" class=\"btn-reset\" onclick=\"this.form.reset()\">重置</button>");
        html.append("</div>");
        html.append("</form>");
        html.append("</div>");
        
        return html.toString();
    }

    /**
     * 根据意图类型生成表单字段
     */
    private List<FormField> generateFormFields(String intentType, Intent intent, UserInfo userInfo) {
        List<FormField> fields = new ArrayList<>();
        
        if ("HOTEL".equals(intentType)) {
            // 优先使用从消息中提取的姓名，如果没有则使用用户信息中的姓名
            String name = intent.getExtractedName();
            if (name == null || name.isEmpty()) {
                name = userInfo.getName();
            }
            fields.add(new FormField("name", "姓名", "text", 
                name, 
                null, true, "请输入姓名"));
            fields.add(new FormField("phone", "联系电话", "text", 
                userInfo.getPhone(), null, true, "请输入联系电话"));
            fields.add(new FormField("email", "邮箱", "text", 
                userInfo.getEmail(), null, false, "请输入邮箱"));
            fields.add(new FormField("destination", "目的地", "text", 
                intent.getExtractedDestination() != null ? intent.getExtractedDestination() : userInfo.getDefaultCity(), 
                null, true, "请输入目的地"));
            fields.add(new FormField("checkInDate", "入住日期", "date", 
                intent.getExtractedDate() != null ? intent.getExtractedDate() : "", 
                null, true, "请选择入住日期"));
            fields.add(new FormField("checkOutDate", "退房日期", "date", "", null, true, "请选择退房日期"));
            fields.add(new FormField("roomType", "房间类型", "select", "", 
                List.of("单人间", "双人间", "大床房", "套房"), true, ""));
            fields.add(new FormField("guests", "入住人数", "number", "1", null, true, "请输入入住人数"));
        } else if ("FLIGHT".equals(intentType)) {
            // 优先使用从消息中提取的姓名，如果没有则使用用户信息中的姓名
            String name = intent.getExtractedName();
            if (name == null || name.isEmpty()) {
                name = userInfo.getName();
            }
            fields.add(new FormField("name", "姓名", "text", 
                name, 
                null, true, "请输入姓名"));
            fields.add(new FormField("phone", "联系电话", "text", 
                userInfo.getPhone(), null, true, "请输入联系电话"));
            fields.add(new FormField("email", "邮箱", "text", 
                userInfo.getEmail(), null, false, "请输入邮箱"));
            fields.add(new FormField("idCard", "身份证号", "text", 
                userInfo.getIdCard(), null, true, "请输入身份证号"));
            fields.add(new FormField("departure", "出发地", "text", 
                userInfo.getDefaultCity(), null, true, "请输入出发地"));
            fields.add(new FormField("destination", "目的地", "text", 
                intent.getExtractedDestination() != null ? intent.getExtractedDestination() : "", 
                null, true, "请输入目的地"));
            fields.add(new FormField("departureDate", "出发日期", "date", 
                intent.getExtractedDate() != null ? intent.getExtractedDate() : "", 
                null, true, "请选择出发日期"));
            fields.add(new FormField("transportation", "交通方式", "select", 
                userInfo.getPreferredTransportation() != null && !userInfo.getPreferredTransportation().isEmpty() 
                    ? userInfo.getPreferredTransportation().get(0) : "飞机", 
                userInfo.getPreferredTransportation(), true, ""));
            fields.add(new FormField("passengers", "乘客人数", "number", "1", null, true, "请输入乘客人数"));
        } else if ("TRAIN".equals(intentType)) {
            // 优先使用从消息中提取的姓名，如果没有则使用用户信息中的姓名
            String name = intent.getExtractedName();
            if (name == null || name.isEmpty()) {
                name = userInfo.getName();
            }
            fields.add(new FormField("name", "姓名", "text", 
                name, 
                null, true, "请输入姓名"));
            fields.add(new FormField("phone", "联系电话", "text", 
                userInfo.getPhone(), null, true, "请输入联系电话"));
            fields.add(new FormField("email", "邮箱", "text", 
                userInfo.getEmail(), null, false, "请输入邮箱"));
            fields.add(new FormField("idCard", "身份证号", "text", 
                userInfo.getIdCard(), null, true, "请输入身份证号"));
            fields.add(new FormField("departure", "出发地", "text", 
                userInfo.getDefaultCity(), null, true, "请输入出发地"));
            fields.add(new FormField("destination", "目的地", "text", 
                intent.getExtractedDestination() != null ? intent.getExtractedDestination() : "", 
                null, true, "请输入目的地"));
            fields.add(new FormField("departureDate", "出发日期", "date", 
                intent.getExtractedDate() != null ? intent.getExtractedDate() : "", 
                null, true, "请选择出发日期"));
            fields.add(new FormField("transportation", "交通方式", "select", 
                userInfo.getPreferredTransportation() != null && !userInfo.getPreferredTransportation().isEmpty() 
                    ? userInfo.getPreferredTransportation().get(0) : "高铁", 
                userInfo.getPreferredTransportation(), true, ""));
            fields.add(new FormField("seatType", "座位类型", "select", "二等座", 
                List.of("一等座", "二等座", "商务座", "硬座", "硬卧", "软卧"), true, ""));
            fields.add(new FormField("passengers", "乘客人数", "number", "1", null, true, "请输入乘客人数"));
        }
        
        return fields;
    }

    /**
     * 生成单个字段的HTML
     */
    private String generateFieldHtml(FormField field) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"form-field\">");
        html.append("<label for=\"").append(field.getName()).append("\">");
        html.append(field.getLabel());
        if (field.isRequired()) {
            html.append("<span class=\"required\">*</span>");
        }
        html.append("</label>");
        
        if ("select".equals(field.getType())) {
            html.append("<select id=\"").append(field.getName()).append("\" name=\"").append(field.getName()).append("\"");
            if (field.isRequired()) {
                html.append(" required");
            }
            html.append(">");
            if (field.getOptions() != null) {
                for (String option : field.getOptions()) {
                    html.append("<option value=\"").append(option).append("\"");
                    if (option.equals(field.getDefaultValue())) {
                        html.append(" selected");
                    }
                    html.append(">").append(option).append("</option>");
                }
            }
            html.append("</select>");
        } else {
            html.append("<input type=\"").append(field.getType()).append("\"");
            html.append(" id=\"").append(field.getName()).append("\"");
            html.append(" name=\"").append(field.getName()).append("\"");
            if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()) {
                html.append(" value=\"").append(escapeHtml(field.getDefaultValue())).append("\"");
            }
            if (field.getPlaceholder() != null && !field.getPlaceholder().isEmpty()) {
                html.append(" placeholder=\"").append(escapeHtml(field.getPlaceholder())).append("\"");
            }
            if (field.isRequired()) {
                html.append(" required");
            }
            html.append(">");
        }
        
        html.append("</div>");
        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}

