package com.leyou;

import com.leyou.order.utils.PayHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LyOrderApplication.class)
public class PayHelperTest {
    @Autowired
    private PayHelper payHelper;

    @Test
    public void testGetPayUrl(){
        String payUrl = payHelper.getPayUrl(1374555262857371650L,1L);
        System.out.println("支付链接："+payUrl);
    }
}
