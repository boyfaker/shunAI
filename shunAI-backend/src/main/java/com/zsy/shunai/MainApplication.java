package com.zsy.shunai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主类（项目启动入口）
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
//  如需开启 Redis，须移除 exclude 中的内容
@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MainApplication {

/**
 * Main entry point of the Spring Boot application.
 * This method is responsible for launching the Spring application context.
 *
 * @param args Command line arguments passed to the application
 * @return SpringApplication instance that has been run
 */
    public static void main(String[] args) {
    // Use the Spring Boot's SpringApplication.run() method to bootstrap and start the application
    // The first parameter is the configuration class (MainApplication.class)
    // The second parameter is the command line arguments
        SpringApplication.run(MainApplication.class, args);
    }

}
