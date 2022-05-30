package com.leyou.gateway.filter;

import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.Payload;
import com.leyou.common.auth.utils.UserInfo;
import com.leyou.common.constants.LyConstants;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.leyou.gateway.scheduler.AppTokenScheduler;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 网关鉴权过滤器
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtProperties jwtProps;
    @Autowired
    private FilterProperties filterProps;
    @Autowired
    private AppTokenScheduler appTokenScheduler;

    /**
     * 编写过滤逻辑
     * @param exchange 封装了request和response
     * @param chain 过滤器链，用于控制过滤器的执行（例如，放行）
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取request和response
        //ServerHttpRequest是Spring提供，对HttpServletRequest对象的二次封装（不是继承关系）
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //在网关的请求头中加入服务token
        request.mutate().header(LyConstants.APP_TOKEN_HEADER,appTokenScheduler.getAppToken());


        //加入请求白名单
        //1）获取当前请求信息（URL）
        String uri = request.getURI().getPath(); // /api/item/category/of/parent
        //2）判断uri是否在白名单中，在的话则放行
        List<String> allowPaths = filterProps.getAllowPaths();
        for(String allowPath:allowPaths){
            if(uri.contains(allowPath)){
                //放行请求
                return chain.filter(exchange);
            }
        }

        //1. 获取请求中的token（Cookie）
        //getCookies():获取浏览器的所有的Cookie
        String token = null;
        try {
            token = request.getCookies().getFirst(jwtProps.getCookie().getCookieName()).getValue();
        } catch (Exception e) {
            //返回状态码401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //注意：必须中止请求
            return response.setComplete();
        }

        if(StringUtils.isEmpty(token)){
            //返回状态码401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //注意：必须中止请求
            return response.setComplete();
        }

        //2. 校验token是否合法（是否可以使用公钥解密）
        Payload<UserInfo> payload = null;

        try {
            payload = JwtUtils.getInfoFromToken(token, jwtProps.getPublicKey(), UserInfo.class);
        } catch (Exception e) {
            //返回状态码401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //注意：必须中止请求
            return response.setComplete();
        }

        //3）从token中取出登录用户信息（登录用户ID）
        UserInfo userInfo = payload.getInfo();
        Long userId = userInfo.getId();

        /**
         * 3）从token中取出登录用户信息（登录用户ID）
         * 4）根据用户ID查询当前用户拥有的权限（用户->角色->权限），得到一个权限列表（RBAC模型）（path和method）
         * 5）获取当前访问的请求信息（请求URL，请求方式）
         * 6)  判断权限列表中是否包含当前请求，如果包含，可以放行；否则，拒绝访问。
         */

        //放行请求
        return chain.filter(exchange);
    }

    /**
     * 过滤器优先级的设置
     *  数值越小，优先级越大
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
