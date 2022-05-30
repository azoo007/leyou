package com.leyou;

import com.leyou.upload.config.OSSProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 上传微服务
 */
@SpringBootApplication
@EnableDiscoveryClient
//@EnableConfigurationProperties(OSSProperties.class)
public class LyUploadApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyUploadApplication.class,args);
    }
}
