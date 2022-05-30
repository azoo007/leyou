package com.leyou.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取OSS相关配置的类
 */
@Data
@Component
@ConfigurationProperties(prefix = "ly.oss")
public class OSSProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String host;
    private String endpoint;
    private String dir;
    private Long expireTime;
    private Long maxFileSize;
}
