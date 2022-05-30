package com.leyou.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("auth-service")
public interface AuthClient {

    /**
     * 微服务鉴权
     */
    @GetMapping("/authorization")
    public String authorization(
            @RequestParam("serviceName") String serviceName,
            @RequestParam("secret") String secret);
}
