package com.leoyu;

import com.leyou.LySmsApplication;
import com.leyou.common.constants.MQConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySmsApplication.class)
public class SmsDemo {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void testSendMsg(){
        Map<String,String> msgMap = new HashMap<>();
        msgMap.put("phone","13677888691");
        msgMap.put("code","5262579");

        amqpTemplate.convertAndSend(
                MQConstants.Exchange.SMS_EXCHANGE_NAME,
                MQConstants.RoutingKey.VERIFY_CODE_KEY,
                msgMap);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
