package com.formdemo.service;

import com.formdemo.model.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserInfoServiceTest {

    private UserInfoService userInfoService;

    @BeforeEach
    void setUp() {
        userInfoService = new UserInfoService();
    }

    @Test
    void testGetUserInfo_WithName() {
        String userName = "jeffery";
        UserInfo userInfo = userInfoService.getUserInfo(userName);
        
        assertNotNull(userInfo);
        assertEquals("jeffery", userInfo.getName());
        assertNotNull(userInfo.getPhone());
        assertNotNull(userInfo.getEmail());
        assertNotNull(userInfo.getPreferredTransportation());
        assertFalse(userInfo.getPreferredTransportation().isEmpty());
    }

    @Test
    void testGetUserInfo_WithNullName() {
        UserInfo userInfo = userInfoService.getUserInfo(null);
        
        assertNotNull(userInfo);
        assertNotNull(userInfo.getName());
        assertEquals("张三", userInfo.getName());
    }

    @Test
    void testGetUserInfo_WithEmptyName() {
        UserInfo userInfo = userInfoService.getUserInfo("");
        
        assertNotNull(userInfo);
        assertNotNull(userInfo.getName());
    }

    @Test
    void testExtractName_WithNamePattern() {
        String input = "我叫jeffery，打算明天去北京";
        String name = userInfoService.extractName(input);
        
        assertNotNull(name);
        assertEquals("jeffery", name);
    }

    @Test
    void testExtractName_WithDifferentPattern() {
        String input = "我是张三";
        String name = userInfoService.extractName(input);
        
        assertNotNull(name);
        assertEquals("张三", name);
    }

    @Test
    void testExtractName_WithoutName() {
        String input = "打算明天去北京";
        String name = userInfoService.extractName(input);
        
        assertNull(name);
    }

    @Test
    void testExtractName_WithNullInput() {
        String name = userInfoService.extractName(null);
        assertNull(name);
    }

    @Test
    void testExtractName_WithEmptyInput() {
        String name = userInfoService.extractName("");
        assertNull(name);
    }
}

