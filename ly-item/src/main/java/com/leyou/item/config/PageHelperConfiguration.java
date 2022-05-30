package com.leyou.item.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置分页拦截器
 */
@Configuration
public class PageHelperConfiguration {

    /**
     * 创建拦截器对象
     * //@Bean: 把返回值对象放入IOC容器
     */
    @Bean
    public PaginationInterceptor createPageHelperConfiguration(){
        return new PaginationInterceptor();
    }

}
