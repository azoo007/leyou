package com.leyou.order.config;

import com.leyou.order.interceptor.UserTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置拦截器
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Autowired
    private UserTokenInterceptor userTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userTokenInterceptor).excludePathPatterns("/wx/notify/**");
    }
}
