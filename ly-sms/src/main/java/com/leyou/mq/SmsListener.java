package com.leyou.mq;

import com.leyou.common.constants.MQConstants;
import com.leyou.utils.SmsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/*
监听短信
 */
@Component
@Slf4j
public class SmsListener {
    @Autowired
    private SmsHelper smsHelper;

    /**
     * 发送短信验证码
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value= MQConstants.Queue.SMS_VERIFY_CODE_QUEUE),
                    exchange = @Exchange(name = MQConstants.Exchange.SMS_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
                    key = MQConstants.RoutingKey.VERIFY_CODE_KEY
            )
    )
    public void sendVerifyCode(Map<String,String> msgMap){
        String phone = msgMap.get("phone");
        String code = msgMap.get("code");
        smsHelper.sendVerifyCode(phone,code);

    }

}
