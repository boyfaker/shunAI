package com.zsy.shunai.config;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Data
public class VipSchedulerConfig {

    @Bean
    public Scheduler vipScheduler() {
        // ThreadFactory创建多线程：可以统一给线程命名，设置守护状态
        // ThreadFactory是接口不能直接new，但是匿名内部类的方法是可以的
        // 创建 ThreadFactory 实例 + 实现接口方法
        ThreadFactory threadFactory = new ThreadFactory() {// 匿名内部类：1.没有类名；2.在创建对象的同时定义类
            // 原子整数类，线程安全的自增、自减操作，给线程池里创建的每个线程一个唯一编号
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            // 使用ThreadFactory创建多线程时必须重写唯一方法newThread
            @Override
            public Thread newThread(Runnable r) {
                // 重写的内容一般是：1.创建线程；2.设置线程名称；3.设置线程是否为守护线程
                Thread t = new Thread(r, "VIPThreadPool-" + threadNumber.getAndIncrement());
                t.setDaemon(false); // 设置为非守护线程
                return t;
            }
        };

        // 上面代码自定义了一个线程

        // 指定核心线程数=10
        // 指定线程的创建方式为自定义的ThreadFactory
        // 线程池在需要线程的时候不直接new Thread而是调用 threadFactory.newThread()
        // 但一般不建议使用Executors直接创建线程池，因为它使用无界队列可能会OOM，线程数理论无上限，可能创建过多线程
        // 可以使用ThreadPoolExecutor
//        ExecutorService executorService = new ThreadPoolExecutor(
//                10, // corePoolSize 核心线程数
//                20, // maximumPoolSize 最大线程数
//                60L, TimeUnit.SECONDS, // keepAliveTime 空闲线程存活时间
//                new ArrayBlockingQueue<>(100), // 有界队列，防止OOM
//                threadFactory, // 自定义线程工厂
//                new ThreadPoolExecutor.AbortPolicy() // 拒绝策略
//        );

        ExecutorService executorService = Executors.newScheduledThreadPool(10, threadFactory);
        // Schedulers.from()方法：将ExecutorService转换为RxJava的Scheduler
        return Schedulers.from(executorService);
    }
}
