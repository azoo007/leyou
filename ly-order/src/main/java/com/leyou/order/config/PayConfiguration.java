package com.leyou.order.config;

import com.github.wxpay.sdk.PayConfig;
import com.github.wxpay.sdk.WXPay;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化支付对象
 */
@Configuration
public class PayConfiguration {

    @Bean
    public WXPay createWxPay(PayProperties payProps) throws Exception {
        PayConfig payConfig = new PayConfig();
        payConfig.setAppID(payProps.getAppId());
        payConfig.setMchID(payProps.getMchId());
        payConfig.setKey(payProps.getKey());
        return new WXPay(payConfig);
    }

}
