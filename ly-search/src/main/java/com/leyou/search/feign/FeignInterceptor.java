package com.leyou.search.feign;

import com.leyou.common.constants.LyConstants;
import com.leyou.search.scheduler.AppTokenScheduler;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 把服务token放入Feign的请求头中
 */
@Component
public class FeignInterceptor implements RequestInterceptor {
    @Autowired
    private AppTokenScheduler appTokenScheduler;

    @Override
    public void apply(RequestTemplate template) {
        template.header(LyConstants.APP_TOKEN_HEADER,appTokenScheduler.getAppToken());
    }
}
