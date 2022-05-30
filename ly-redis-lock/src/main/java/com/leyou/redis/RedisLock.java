package com.leyou.redis;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * redis分布式锁
 */
public class RedisLock {

    public StringRedisTemplate redisTemplate;

    private String key;

    private String value="1";

    public RedisLock(String key,StringRedisTemplate redisTemplate){
        this.key = key;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取锁
     */
    public Boolean lock(Long timeout){
        return redisTemplate.opsForValue().setIfAbsent(key,value,timeout, TimeUnit.SECONDS);
    }


    /**
     * 释放锁
     */
    public void unlock(){
        redisTemplate.delete(key);
    }

}
