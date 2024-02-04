package com.jinchan.utils;

import com.jinchan.properties.FitBuddyProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @Author jinchan
 */
@Component
public class MessageUtils {
    private static FitBuddyProperties fitBuddyProperties;

    @Resource
    private FitBuddyProperties tempProperties;


    public static void sendMessage(String phoneNum, String code) {
        if (fitBuddyProperties.isUseShortMessagingService()) {
            SMSUtils.sendMessage(phoneNum, code);
        } else {
            System.out.println("验证码: " + code);
        }
    }

    @PostConstruct
    public void initProperties() {
        fitBuddyProperties = tempProperties;
    }
}
