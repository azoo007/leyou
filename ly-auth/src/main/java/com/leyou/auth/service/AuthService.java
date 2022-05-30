package com.leyou.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.mapper.ApplicationInfoMapper;
import com.leyou.auth.pojo.ApplicationInfo;
import com.leyou.common.auth.utils.AppInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.Payload;
import com.leyou.common.auth.utils.UserInfo;
import com.leyou.common.constants.LyConstants;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.client.UserClient;
import com.leyou.user.pojo.User;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties jwtProps;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ApplicationInfoMapper applicationInfoMapper;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    public void login(String username, String password, HttpServletRequest request, HttpServletResponse response) {
        //1.判断用户名和密码是否正确
        User loginUser = userClient.query(username,password);

        //2.使用JwtUtils工具生成token字符串
        UserInfo userInfo = new UserInfo(loginUser.getId(),loginUser.getUsername(),"admin");

        //3.生成token，写回token给浏览器
        buildTokenAndWriteCookie(response, userInfo);
    }

    /**
     * 生成token，写回token给浏览器
     * @param response
     * @param userInfo
     */
    public void buildTokenAndWriteCookie(HttpServletResponse response, UserInfo userInfo) {
        String token = JwtUtils.generateTokenExpireInMinutes(
                userInfo,
                jwtProps.getPrivateKey(),
                jwtProps.getCookie().getExpire());

        //3.把token以Cookie形式写回给浏览器
        CookieUtils.newCookieBuilder()
                .response(response)
                .name(jwtProps.getCookie().getCookieName())
                .value(token)
                .domain(jwtProps.getCookie().getCookieDomain())
                .maxAge(jwtProps.getCookie().getExpire()*60) // 注意：Cookie的时间以秒单位
                .build();
    }

    public UserInfo verify(HttpServletRequest request, HttpServletResponse response) {
        //1.取出Cookie的token
        String token = CookieUtils.getCookieValue(request, jwtProps.getCookie().getCookieName());

        //2.校验token是否合法
        Payload<UserInfo> payload = null;

        try {
            payload = JwtUtils.getInfoFromToken(token, jwtProps.getPublicKey(), UserInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

        //加入判断该token是否在黑名单
        if(redisTemplate.hasKey(LyConstants.REDIS_TOKEN_PRE+payload.getId())){
            //拒绝访问
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

        //3.取出token的用户信息，并返回
        UserInfo userInfo = payload.getInfo();

        //加入刷新token（刷新用户登录状态）代码
        //1.获取token的过期时间
        Date expiration = payload.getExpiration();

        //2.计算刷新时间点（过期时间-15分钟）
        DateTime refreshTime = new DateTime(expiration).minusMinutes(jwtProps.getCookie().getRefreshTime());

        //3.判断刷新时间点<当前时间，则重新生成token，写cookie回浏览器
        if(refreshTime.isBeforeNow()){
            //把旧的token放入黑名单
            addToBackList(payload);

            //刷新token
            buildTokenAndWriteCookie(response,userInfo);
        }

        return userInfo;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        //1.获取token
        String token = CookieUtils.getCookieValue(request, jwtProps.getCookie().getCookieName());

        //2.校验token
        Payload<UserInfo> payload = null;

        try {
            payload = JwtUtils.getInfoFromToken(token, jwtProps.getPublicKey(), UserInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

        //3.把token的ID存入redis，过期时间为剩余有效期
        addToBackList(payload);

        //4.删除token
        CookieUtils.deleteCookie(
                jwtProps.getCookie().getCookieName(),
                jwtProps.getCookie().getCookieDomain(),
                response);

    }

    /**
     * 把token放入黑名单
     * @param payload
     */
    public void addToBackList(Payload<UserInfo> payload) {
        //1）取出token的id
        String tokenId = payload.getId();
        //2）获取token的过期时间
        Date expiration = payload.getExpiration();
        //3）计算剩余过期时间
        Long remainTime = expiration.getTime()-System.currentTimeMillis();
        //4）存入redis
        redisTemplate.opsForValue().set(LyConstants.REDIS_TOKEN_PRE+tokenId,"1",remainTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 验证服务名称和服务密钥是否正确
     */
    public ApplicationInfo checkServiceNamdAndSecret(String serviceName,String secret){
        //1.判断服务名称是否存在
        ApplicationInfo info = new ApplicationInfo();
        info.setServiceName(serviceName);
        QueryWrapper<ApplicationInfo> queryWrapper = Wrappers.query(info);
        ApplicationInfo applicationInfo = applicationInfoMapper.selectOne(queryWrapper);

        if(applicationInfo==null){
            throw new LyException(ExceptionEnum.INVALID_SERVER_ID_SECRET);
        }

        //2.判断密码是否正确
        if(!passwordEncoder.matches(secret,applicationInfo.getSecret())){
            throw new LyException(ExceptionEnum.INVALID_SERVER_ID_SECRET);
        }
        return applicationInfo;
    }

    public String authorization(String serviceName, String secret) {
        //1.判断服务名称和密钥是否正确
        ApplicationInfo applicationInfo = checkServiceNamdAndSecret(serviceName,secret);

        //2.查询当前服务调用的目标服务列表
        List<String> targetList = applicationInfoMapper.queryTargetList(applicationInfo.getId());

        //3.生成服务的token
        AppInfo appInfo = new AppInfo(applicationInfo.getId(),applicationInfo.getServiceName(),targetList);

        String token = JwtUtils.generateTokenExpireInMinutes(appInfo, jwtProps.getPrivateKey(), jwtProps.getApp().getExpire());

        //4.返回服务的token
        return token;
    }
}
