package com.ita.home.config;

import com.ita.home.consumer.EmailConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/23 19:23
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ThreadPoolConfig {
    private final EmailConsumer emailConsumer;

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
        executor.setCorePoolSize(2);
        // 最大线程数
        executor.setMaxPoolSize(4);
        // 队列容量（与EmailQueue一致，避免双重队列）
        executor.setQueueCapacity(1000);
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
}
