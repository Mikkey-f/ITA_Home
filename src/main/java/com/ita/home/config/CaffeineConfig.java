package com.ita.home.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ita.home.model.entity.UserPlatformRanking;
import com.ita.home.model.vo.OjUserDataVo;
import com.ita.home.model.vo.RankingPageVo;
import com.ita.home.model.vo.UserPlatformRankingVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
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

    /**
     * 验证码缓存管理器：key=邮箱（String），value=验证码（Integer）
     * 有效期10分钟，自动过期（无需手动清理）
     */
    @Bean("verifyCodeCacheManager")
    public CacheManager verifyCodeCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // 配置缓存规则：30分钟过期，初始容量100（适配小访问量）
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES) // 写入后10分钟过期
                .initialCapacity(100)
                .maximumSize(maxSize) // 最大缓存1万条（避免内存溢出）
        );
        return cacheManager;
    }

    // 新增：排名专用缓存（直接返回Cache对象，避免类型冲突）
    @Bean("platformRankingCache")
    public Cache<String, UserPlatformRankingVo> platformRankingCache() {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

}


