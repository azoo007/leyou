package com.leyou.search.interceptor;

import com.leyou.common.auth.utils.AppInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.Payload;
import com.leyou.common.constants.LyConstants;
import com.leyou.search.config.JwtProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 服务token验证拦截器
 */
@Component
public class AppTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProps;

    /**
     * 在Controller方法执行之前 被调用
     * @param request
     * @param response
     * @param handler
     * @return  true： 放行  false：拒绝访问
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取请求的token
        String appToken = request.getHeader(LyConstants.APP_TOKEN_HEADER);

        if(StringUtils.isEmpty(appToken)){
            //拒绝访问
            return false;
        }

        //2.验证token的合法性
        Payload<AppInfo> payload = null;

        try {
            payload = JwtUtils.getInfoFromToken(appToken, jwtProps.getPublicKey(), AppInfo.class);
        } catch (Exception e) {
            //拒绝访问
            return false;
        }

        //3.取出token的目标服务列表
        AppInfo appInfo = payload.getInfo();
        List<String> targetList = appInfo.getTargetList();

        //4.判断当前服务是否在目标服务列表中
        if(!targetList.contains(jwtProps.getApp().getServiceName())){
            //拒绝访问
            return false;
        }

        //放行
        return true;
    }
}
