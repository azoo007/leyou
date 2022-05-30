package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LyRedisLockApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyRedisLockApplication.class,args);
    }
}
