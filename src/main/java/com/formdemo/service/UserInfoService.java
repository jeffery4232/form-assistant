package com.formdemo.service;

import com.formdemo.model.UserInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class UserInfoService {

    /**
     * 模拟调用外部API获取用户信息
     * 实际应用中这里会调用真实的API
     */
    public UserInfo getUserInfo(String userName) {
        // 模拟API调用延迟
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 模拟返回用户信息
        UserInfo userInfo = new UserInfo();
        if(userName != null  && userName.equals("jeffery")){
            userInfo.setName(userName);
            userInfo.setPhone("138****8888");
            userInfo.setEmail("user@example.com");
            userInfo.setIdCard("110101199001011234");
            userInfo.setPreferredTransportation(Arrays.asList("飞机", "高铁", "自驾", "大巴"));
            userInfo.setDefaultCity("北京");
        }


        return userInfo;
    }

    /**
     * 从用户输入中提取姓名
     */
    public String extractName(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return null;
        }

        // 模式1: 我叫/我是/姓名/名字 + 姓名
        String[] patterns1 = {"我叫", "我是", "姓名", "名字"};
        for (String pattern : patterns1) {
            int index = userInput.indexOf(pattern);
            if (index != -1) {
                String remaining = userInput.substring(index + pattern.length()).trim();
                // 提取后面的1-10个字符作为姓名
                String[] parts = remaining.split("[，,。.\\s]");
                if (parts.length > 0 && parts[0].length() <= 10) {
                    return parts[0];
                }
            }
        }

        // 模式2: 帮...定/订/买
        java.util.regex.Pattern pattern2 = java.util.regex.Pattern.compile("帮([\\u4e00-\\u9fa5a-zA-Z]+)(?:定|订|买)");
        java.util.regex.Matcher matcher2 = pattern2.matcher(userInput);
        if (matcher2.find()) {
            return matcher2.group(1);
        }

        // 模式3: 为...定/订/买
        java.util.regex.Pattern pattern3 = java.util.regex.Pattern.compile("为([\\u4e00-\\u9fa5a-zA-Z]+)(?:定|订|买)");
        java.util.regex.Matcher matcher3 = pattern3.matcher(userInput);
        if (matcher3.find()) {
            return matcher3.group(1);
        }

        // 模式4: 给...定/订/买
        java.util.regex.Pattern pattern4 = java.util.regex.Pattern.compile("给([\\u4e00-\\u9fa5a-zA-Z]+)(?:定|订|买)");
        java.util.regex.Matcher matcher4 = pattern4.matcher(userInput);
        if (matcher4.find()) {
            return matcher4.group(1);
        }

        return null;
    }
}
