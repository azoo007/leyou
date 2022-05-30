package com.leyou.user.controller;

import com.leyou.common.exception.pojo.LyException;
import com.leyou.user.pojo.AddressDTO;
import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 校验用户名和手机号是否唯一
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data,@PathVariable("type") Integer type){
        Boolean isCanUse = userService.checkData(data,type);
        return ResponseEntity.ok(isCanUse);
    }

    /**
     * 发送手机验证码
     */
    @PostMapping("/code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam("phone") String phone){
        userService.sendVerifyCode(phone);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid User user, BindingResult result, @RequestParam("code") String code){
        //自定义服务验证错误信息
        if(result.hasErrors()){
            String errorMsg = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(Collectors.joining("|"));
            throw new LyException(500,errorMsg);
        }

        userService.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 检查用户名和密码是否正确
     */
    @GetMapping("/query")
    public ResponseEntity<User> query(@RequestParam("username") String username,@RequestParam("password") String password){
        User loginUser = userService.query(username,password);
        return ResponseEntity.ok(loginUser);
    }

    /**
     * 查询指定收货地址信息
     */
    @GetMapping("/address")
    public ResponseEntity<AddressDTO> findAddressById(
            @RequestParam("userId") Long userId,
            @RequestParam("id") Long id
    ){
        AddressDTO addressDTO = userService.findAddressById(userId,id);
        return ResponseEntity.ok(addressDTO);
    }
}
