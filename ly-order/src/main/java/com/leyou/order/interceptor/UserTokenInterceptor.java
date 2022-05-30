package com.leyou.order.interceptor;

import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.Payload;
import com.leyou.common.auth.utils.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.UserHolder;
import com.leyou.order.config.JwtProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 获取登录用户信息拦截器
 */
@Component
public class UserTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProps;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取token
        String token = CookieUtils.getCookieValue(request, jwtProps.getCookie().getCookieName());

        if(StringUtils.isEmpty(token)){
            return false;
        }

        //2.验证token
        Payload<UserInfo> payload = null;

        try {
            payload = JwtUtils.getInfoFromToken(token,jwtProps.getPublicKey(), UserInfo.class);
        } catch (Exception e) {
            return false;
        }

        //3.取出UserInfo
        UserInfo userInfo = payload.getInfo();

        //4.存入ThreadLocal
        UserHolder.setUser(userInfo);

        return true;
    }
}
