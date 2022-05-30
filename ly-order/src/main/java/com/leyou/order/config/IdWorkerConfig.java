package com.leyou.order.config;

import com.leyou.common.utils.IdWorker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdWorkerConfig {

    @Bean
    public IdWorker createIdWorker(IdWorkProperties idWorkProps){
        return new IdWorker(
                idWorkProps.getWorkerId(),
                idWorkProps.getDataCenterId());
    }

}
