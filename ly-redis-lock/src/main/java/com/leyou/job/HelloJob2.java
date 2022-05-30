package com.leyou.job;
import com.leyou.redis.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 模拟业务执行过程
 */
@Slf4j
@Component
public class HelloJob2 {

    @Autowired
    private RedissonClient redissonClient;


    @Scheduled(cron = "0/10 * * * * ?")
    public void hello() throws InterruptedException {
        //1.创建锁对象
        RLock rLock = redissonClient.getLock("102");

        //2.获取锁
        boolean isLock = rLock.tryLock(10, TimeUnit.SECONDS);

        if(!isLock){
            log.info("获取锁失败");
        }

        try {
            // 执行业务
            log.info("获取锁成功，开始执行扣减库存。");
            // 模拟任务耗时
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("任务执行异常", e);
        } finally {
            //释放锁
            rLock.unlock();
        }
    }
}
