package com.ita.home.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ita.home.model.vo.OjUserDataVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/25 15:23
 */
@Configuration
@Slf4j
public class CaffeineConfig {

    @Value("${ita.oj.cache.max-size}")
    private Long maxSize;

    @Value("${ita.oj.cache.expire-hours}")
    private Integer expireHours;

    @Value("${ita.oj.cache.active-user-days}")
    private Integer activeUserDays;

    @Value("${ita.oj.cache.async-update-timeout-seconds}")
    private Integer asyncUpdateTimeoutSeconds;

    /**
     * key: oj_update_lock:123
     * value: OjUserDataVo 对象
     * 表示当前缓存中，用户oj所缓存的UserDataVo对象
     */
    @Bean
    public Cache<String, OjUserDataVo> ojDataCache() {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireHours, TimeUnit.HOURS)
                .recordStats() // 开启统计
                .removalListener((key, value, cause) -> {
                    log.debug("缓存被移除: key={}, cause={}", key, cause);
                })
                .build();
    }

    /**
     * key: oj_update_lock:123
     * value: 线程的名称
     * 表示当前用户oj锁被哪一个线程占据
     */
    @Bean
    public Cache<String, String> updateLockCache() {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(asyncUpdateTimeoutSeconds, TimeUnit.MINUTES) // 防止锁泄露
                .build();
    }
}


