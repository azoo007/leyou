package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 搜索微服务
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients //开启Feign
@EnableScheduling
public class LySearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(LySearchApplication.class,args);
    }
}
