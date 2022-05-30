package com.leyou.page.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置MQ的消息转换器
 */
@Configuration
public class RabbitConfig {

    @Bean
    public Jackson2JsonMessageConverter createMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
