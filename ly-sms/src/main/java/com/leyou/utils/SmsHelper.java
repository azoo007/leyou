package com.leyou.utils;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.leyou.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 短信工具类
 */
@Component
@Slf4j
public class SmsHelper {
    @Autowired
    private Client client;
    @Autowired
    private SmsProperties smsProps;

    /**
     * 发送短信验证码
     */
    public void sendVerifyCode(String phone,String code){
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(smsProps.getSignName())
                .setTemplateCode(smsProps.getVerifyCodeTemplate())
                .setTemplateParam("{\""+smsProps.getCode()+"\":\""+code+"\"}");
        try {
            SendSmsResponse smsResponse = client.sendSms(sendSmsRequest);

            //获取状态码
            String respCode = smsResponse.body.code;
            //获取响应消息
            String respMsg = smsResponse.body.message;

            if(respCode.equals("OK")){
                log.info("【短信API】短信发送成功");
            }else{
                log.error("【短信API】短信发送失败：原因："+respMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【短信API】短信发送失败：原因："+e.getMessage());
        }
    }
}
