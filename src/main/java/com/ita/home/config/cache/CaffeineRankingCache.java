package com.ita.home.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.ita.home.model.vo.UserPlatformRankingVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/26 16:02
 */
@Component
@Slf4j
public class CaffeineRankingCache {
    // 直接注入Cache对象，而不是CacheManager
    private final Cache<String, UserPlatformRankingVo> platformRankingCache;

    public CaffeineRankingCache(
            @Qualifier("platformRankingCache") Cache<String, UserPlatformRankingVo> platformRankingCache) {
        this.platformRankingCache = platformRankingCache;
    }

    /**
     * 获取平台排名缓存
     */
    public Optional<UserPlatformRankingVo> getRanking(String platformId, Long userId) {
        String key = buildCacheKey(platformId, userId);
        UserPlatformRankingVo cached = platformRankingCache.getIfPresent(key);

        if (cached != null) {
            log.debug("Caffeine平台排名缓存命中: {}", key);
        }

        return Optional.ofNullable(cached);
    }

    /**
     * 存储平台排名缓存
     */
    public void putRanking(String platformId, Long userId, UserPlatformRankingVo ranking) {
        String key = buildCacheKey(platformId, userId);
        platformRankingCache.put(key, ranking);
        log.debug("Caffeine平台排名缓存存储: {}", key);
    }

    /**
     * 生成缓存Key
     */
    private String buildCacheKey(String platformId, Long userId) {
        return String.format("ranking:%s:%d", platformId, userId);
    }


}
