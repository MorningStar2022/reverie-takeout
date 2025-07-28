package com.whurs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement//开启注解方式的事务管理
@EnableCaching//开启注解方式的缓存策略
@Slf4j
@EnableScheduling//开启任务调度注解
public class ReverieServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReverieServerApplication.class, args);
    }

}
