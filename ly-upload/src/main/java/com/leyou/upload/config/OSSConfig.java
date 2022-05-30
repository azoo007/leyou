package com.leyou.upload.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建好OSS签名代码需要的初始对象
 */
@Configuration
public class OSSConfig {

    @Bean
    public OSS createOSS(OSSProperties ossProps){
        return new OSSClientBuilder().build(
                ossProps.getEndpoint(),
                ossProps.getAccessKeyId(),
                ossProps.getAccessKeySecret());
    }

}
