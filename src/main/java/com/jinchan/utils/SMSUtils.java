package com.jinchan.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.jinchan.properties.SMSProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * ClassName: 短信发送工具
 * Package: com.jinchan.utils
 * Description:
 *
 * @Author jinchan
 * @Create 2024/1/27 20:29
 */
@Log4j2
@Component
public class SMSUtils {
    private static SMSProperties smsProperties;
    @Resource
    private SMSProperties tempProperties;
    //todo 自己的阿里云配置
    public static void sendMessage(String phoneNum, String code) {
        IClientProfile profile = DefaultProfile.getProfile(smsProperties.getRegionId(), smsProperties.getAccessKey(), smsProperties.getSecretKey());
        IAcsClient client = new DefaultAcsClient(profile);
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phoneNum);
        request.setSignName(smsProperties.getSignName());
        request.setTemplateCode(smsProperties.getTemplateCode());
        request.setTemplateParam("{" + smsProperties.getTemplateParam() + ":\"" + code + "\"}");
        try {
            SendSmsResponse response = client.getAcsResponse(request);
            log.info("发送结果: " + response.getMessage());
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }
    @PostConstruct
    public void initProperties() {
        smsProperties = tempProperties;
    }
}
