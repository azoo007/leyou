package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 网关微服务
 */
/*@SpringBootApplication
@EnableDiscoveryClient  //注册配置
@EnableCircuitBreaker  //熔断配置*/

@SpringCloudApplication
@EnableFeignClients //开启Feign
@EnableScheduling //开启SpringTask
public class LyGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyGatewayApplication.class,args);
    }
}
