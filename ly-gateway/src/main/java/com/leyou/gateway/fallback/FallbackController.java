package com.leyou.gateway.fallback;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 熔断降级处理
 */
@RestController
public class FallbackController {

    /**
     * 降级处理路径
     */
    @RequestMapping("/fallback")
    public String fallback(){
        return "服务器繁忙，请稍后再来";
    }
}
