package com.leyou.item.config;

import com.leyou.item.interceptor.AppTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 把拦截器放入环境中
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Autowired
    private AppTokenInterceptor appTokenInterceptor;

    /**
     * 用于添加拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * addPathPatterns(): 给拦截器添加拦截路径 （默认值：/  或  /**）
         * excludePathPatterns(): 给拦截器添加放行路径
         */
        registry.addInterceptor(appTokenInterceptor);
    }
}
