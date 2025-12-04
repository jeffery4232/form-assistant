package com.formdemo.service;

import com.formdemo.model.FormField;
import com.formdemo.model.Intent;
import com.formdemo.model.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormGeneratorServiceTest {

    private FormGeneratorService formGeneratorService;

    @BeforeEach
    void setUp() {
        formGeneratorService = new FormGeneratorService();
    }

    @Test
    void testGenerateFormHtml_ForHotel() {
        Intent intent = new Intent();
        intent.setType("HOTEL");
        intent.setExtractedName("jeffery");
        intent.setExtractedDestination("北京");
        intent.setExtractedDate("明天");
        
        UserInfo userInfo = new UserInfo();
        userInfo.setName("jeffery");
        userInfo.setPhone("138****8888");
        userInfo.setEmail("test@example.com");
        userInfo.setPreferredTransportation(Arrays.asList("飞机", "高铁"));
        
        String html = formGeneratorService.generateFormHtml("HOTEL", intent, userInfo);
        
        assertNotNull(html);
        assertFalse(html.isEmpty());
        assertTrue(html.contains("form-container"));
        assertTrue(html.contains("姓名"));
        assertTrue(html.contains("目的地"));
        assertTrue(html.contains("入住日期"));
    }

    @Test
    void testGenerateFormHtml_ForFlight() {
        Intent intent = new Intent();
        intent.setType("FLIGHT");
        intent.setExtractedName("jeffery");
        intent.setExtractedDestination("北京");
        
        UserInfo userInfo = new UserInfo();
        userInfo.setName("jeffery");
        userInfo.setPhone("138****8888");
        userInfo.setEmail("test@example.com");
        userInfo.setPreferredTransportation(Arrays.asList("飞机", "高铁"));
        
        String html = formGeneratorService.generateFormHtml("FLIGHT", intent, userInfo);
        
        assertNotNull(html);
        assertFalse(html.isEmpty());
        assertTrue(html.contains("form-container"));
        assertTrue(html.contains("出发地"));
        assertTrue(html.contains("目的地"));
        assertTrue(html.contains("交通方式"));
    }

    @Test
    void testGenerateFormHtml_ForTrain() {
        Intent intent = new Intent();
        intent.setType("TRAIN");
        intent.setExtractedName("jeffery");
        intent.setExtractedDestination("北京");
        
        UserInfo userInfo = new UserInfo();
        userInfo.setName("jeffery");
        userInfo.setPhone("138****8888");
        userInfo.setEmail("test@example.com");
        userInfo.setPreferredTransportation(Arrays.asList("高铁", "火车"));
        
        String html = formGeneratorService.generateFormHtml("TRAIN", intent, userInfo);
        
        assertNotNull(html);
        assertFalse(html.isEmpty());
        assertTrue(html.contains("form-container"));
        assertTrue(html.contains("座位类型"));
    }
}

