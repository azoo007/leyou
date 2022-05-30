package com.leyou.page.scheduler;

import com.leyou.auth.client.AuthClient;
import com.leyou.page.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时向授权中心申请服务token
 */
@Component
@Slf4j
public class AppTokenScheduler {
    @Autowired
    private AuthClient authClient;
    @Autowired
    private JwtProperties jwtProps;

    //设计一个成员变量，用于存放当前服务的token
    private String appToken;

    /**
     * token刷新间隔
     */
    private static final long TOKEN_REFRESH_INTERVAL = 86400000L; //24小时

    /**
     * token获取失败后重试的间隔
     */
    private static final long TOKEN_RETRY_INTERVAL = 10000L;

    /**
     * 固定频率（1天调用1次）
     */
    @Scheduled(fixedRate = TOKEN_REFRESH_INTERVAL)
    public void appToken(){
        //需要设计重试机制，避免1次申请不到没有token可用的情况

        while(true) {
            try {
                //向授权中心申请服务token
                String appToken = authClient.authorization(
                        jwtProps.getApp().getServiceName(),
                        jwtProps.getApp().getSecret());

                //进行token赋值
                this.appToken = appToken;

                log.info("【申请token】申请token成功，服务名称："+jwtProps.getApp().getServiceName());
                //一旦toke获取成功，则退出
                break;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("【申请token】申请token失败，10秒后重试...");
                //10秒后重试
                try {
                    Thread.sleep(TOKEN_RETRY_INTERVAL);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    //提供get方法给外部获取服务token
    public String getAppToken() {
        return appToken;
    }
}
