package com.leyou.job;
import com.leyou.redis.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 模拟业务执行过程
 */
@Slf4j
//@Component
public class HelloJob {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Scheduled(cron = "0/10 * * * * ?")
    public void hello() {
        //1.创建锁对象
        RedisLock rLock = new RedisLock("102",redisTemplate);

        //2.获取锁
        Boolean isLock = rLock.lock(10L);

        if(!isLock){
            log.info("获取锁失败");
            return;
        }

        try {
            // 执行业务
            log.info("获取锁成功，开始执行扣减库存。");
            // 模拟任务耗时
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("任务执行异常", e);
        } finally {
            //3.释放锁
            rLock.unlock();
        }
    }
}
