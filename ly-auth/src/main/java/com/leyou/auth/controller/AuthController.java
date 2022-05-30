package com.leyou.auth.controller;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.utils.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 授权
 */
@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response){
        authService.login(username,password,request,response);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 验证用户，并返回
     */
    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(HttpServletRequest request,HttpServletResponse response){
        UserInfo userInfo = authService.verify(request,response);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,HttpServletResponse response){
        authService.logout(request,response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 微服务鉴权
     */
    @GetMapping("/authorization")
    public ResponseEntity<String> authorization(
            @RequestParam("serviceName") String serviceName,
            @RequestParam("secret") String secret){
        String token = authService.authorization(serviceName,secret);
        return ResponseEntity.ok(token);
    }
}
