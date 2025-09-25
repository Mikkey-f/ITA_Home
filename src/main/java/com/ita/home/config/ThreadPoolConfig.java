package com.ita.home.config;

import com.ita.home.consumer.EmailConsumer;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/23 19:23
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ThreadPoolConfig {
    private final EmailConsumer emailConsumer;

    // 从配置文件读取参数（默认值可根据实际场景调整）
    @Value("${thread-pool.oj-api.core-pool-size}")
    private int corePoolSize;

    @Value("${thread-pool.oj-api.max-pool-size}")
    private int maxPoolSize;

    @Value("${thread-pool.oj-api.queue-capacity}")
    private int queueCapacity;

    @Value("${thread-pool.oj-api.keep-alive-seconds}")
    private int keepAliveSeconds;

    /**
     * 配置邮箱发送线程池
     * 核心线程数：2（根据邮件发送峰值调整，不宜过多，避免触发邮箱服务商限流）
     * 最大线程数：4
     * 队列容量：1000（与EmailQueue一致）
     * 拒绝策略：CallerRunsPolicy（队列满时，由调用者线程执行，避免任务丢失）
     */
    @Bean(name = "emailThreadPool")
    public Executor emailThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 队列容量（与EmailQueue一致，避免双重队列）
        executor.setQueueCapacity(queueCapacity);
        // 线程名称前缀（便于日志排查）
        executor.setThreadNamePrefix("Email-Consumer-");
        // 拒绝策略：队列满时，调用者线程执行（如注册线程）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化线程池
        executor.initialize();
        // 关键：启动消费者线程（核心线程数=2，启动2个消费者）
        for (int i = 0; i < executor.getCorePoolSize(); i++) {
            executor.submit(emailConsumer);
            log.info("邮箱消费者线程启动：{}", i + 1);
        }
        return executor;
    }


    /**
     * 用于并行调用OJ平台API的线程池
     */
    @Bean(name = "ojApiExecutorService") // 指定Bean名称，便于注入时区分
    public ExecutorService ojApiExecutorService() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity), // 任务队列
                new ThreadFactory() { // 自定义线程工厂，便于日志追踪
                    private int count = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("oj-api-thread-" + count++); // 线程名称带前缀，便于识别
                        thread.setDaemon(false); // 非守护线程，避免任务未完成被强制终止
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：让提交任务的线程执行，避免任务丢失
        );
    }
}
