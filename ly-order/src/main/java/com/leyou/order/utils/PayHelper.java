package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.order.config.PayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付工具类
 */
@Component
@Slf4j
public class PayHelper {
    @Autowired
    private PayProperties payProps;
    @Autowired
    private WXPay wxPay;

    /**
     * 对接微信支付获取二维码链接
     */
    public String getPayUrl(Long orderId,Long totalFee){
        // 请求参数：
        Map<String, String> data = new HashMap<String, String>();
        data.put("body", "乐优商城-支付订单");
        data.put("out_trade_no", orderId.toString());
        data.put("total_fee", totalFee.toString());
        data.put("spbill_create_ip", "123.12.12.123");
        data.put("notify_url", payProps.getNotifyUrl());
        data.put("trade_type", payProps.getPayType());  // 此处指定为扫码支付

        try {
            Map<String, String> resp = wxPay.unifiedOrder(data);

            //判断获取链接是否成功
            //获取return_code 和 result_code
            String returnCode =resp.get("return_code");
            String resultCode =resp.get("result_code");
            if(returnCode.equals("SUCCESS") && resultCode.equals("SUCCESS")){
                log.info("【申请微信支付链接】申请链接成功");
                return resp.get("code_url");
            }else{
                log.error("【申请微信支付链接】申请链接失败，原因："+resp.get("return_msg"));
                throw new LyException(500,"【申请微信支付链接】申请链接失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【申请微信支付链接】申请链接失败，原因："+e.getMessage());
            throw new LyException(500,"【申请微信支付链接】申请链接失败");
        }
    }

}
