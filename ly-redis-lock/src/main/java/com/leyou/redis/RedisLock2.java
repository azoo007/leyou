package com.leyou.redis;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * redis分布式锁2
 */
public class RedisLock2 {

    public StringRedisTemplate redisTemplate;

    private String key;

    private String value;

    public RedisLock2(String key, StringRedisTemplate redisTemplate){
        this.key = key;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取锁
     */
    public Boolean lock(Long timeout){
        //获取当前线程ID
        value = Thread.currentThread().getId()+"";
        return redisTemplate.opsForValue().setIfAbsent(key,value,timeout, TimeUnit.SECONDS);
    }


    /**
     * 释放锁
     */
    public void unlock(){
        //从redis取出vaule
        String redisThreadId = redisTemplate.opsForValue().get(key);
        //获取当前线程ID
        String curThreadId = Thread.currentThread().getId()+"";

        if(curThreadId.equals(redisThreadId)){
            //只有释放锁的线程 是 当初获取锁的线程，才可以释放该锁。
            redisTemplate.delete(key);
        }

    }

}
