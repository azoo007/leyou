package com.leyou.cart.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * 读取Jwt的配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {
    private String pubKeyPath;

    private PublicKey publicKey;//公钥对象

    //注意：这里不能使用构造方法初始化数据
    @PostConstruct // 定义为初始化方法
    public void initMethod() throws Exception {
        publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }


    //接收cookie数据
    private CookiePojo cookie = new CookiePojo();

    private AppPojo app = new AppPojo();

    @Data
    public class CookiePojo{
        private String cookieName;
    }

    @Data
    public class AppPojo{
        private String serviceName;
        private String secret;
    }
}
