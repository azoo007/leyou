package com.leyou.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.leyou.common.constants.LyConstants;
import com.leyou.common.constants.MQConstants;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.AddressDTO;
import com.leyou.user.pojo.User;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Boolean checkData(String data, Integer type) {
        //1.封装条件
        User user = new User();
        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
        }
        QueryWrapper<User> queryWrapper = Wrappers.query(user);

        //2.执行查询，返回数据
        return userMapper.selectCount(queryWrapper)==0;
    }

    public void sendVerifyCode(String phone) {
        //1.生成随机6位数字的验证码
        String code = RandomStringUtils.randomNumeric(6);

        //2.把验证码存入redis，设置5分钟过期
        redisTemplate.opsForValue().set(LyConstants.REDIS_KEY_PRE+phone,code,5, TimeUnit.MINUTES);

        //3.发消息给MQ
        Map<String,String> msMap = new HashMap<>();
        msMap.put("phone",phone);
        msMap.put("code",code);

        amqpTemplate.convertAndSend(
                MQConstants.Exchange.SMS_EXCHANGE_NAME,
                MQConstants.RoutingKey.VERIFY_CODE_KEY,
                msMap
        );
    }

    public void register(User user, String code) {
        //1.取出redis保存的验证码
        String redisCode = redisTemplate.opsForValue().get(LyConstants.REDIS_KEY_PRE + user.getPhone());

        //2.判断和用户输入的验证码是否一致
        if(redisCode==null || !redisCode.equals(code)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }

        try {
            //3.对密码加盐加密
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            //4.保存用户表数据
            userMapper.insert(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    public User query(String username, String password) {
        //1.查询用户名是否存在
        User user = new User();
        user.setUsername(username);
        QueryWrapper<User> queryWrapper = Wrappers.query(user);
        User loginUser = userMapper.selectOne(queryWrapper);

        if(loginUser==null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        //2.判断密码是否正确
        if(!passwordEncoder.matches(password,loginUser.getPassword())){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        return loginUser;
    }

    public AddressDTO findAddressById(Long userId, Long id) {
        AddressDTO address = new AddressDTO();
        address.setId(1L);
        address.setStreet("珠吉路58号津安创业园一层黑马程序员");
        address.setCity("广州");
        address.setDistrict("天河区");
        address.setAddressee("小飞飞");
        address.setPhone("15800000000");
        address.setProvince("广东");
        address.setPostcode("510000");
        address.setIsDefault(true);
        return address;
    }
}
