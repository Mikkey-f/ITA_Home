package com.ita.home.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/23 19:49
 */
@Configuration
public class CacheConfig {
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
                .maximumSize(10000) // 最大缓存1万条（避免内存溢出）
        );
        return cacheManager;
    }

}
