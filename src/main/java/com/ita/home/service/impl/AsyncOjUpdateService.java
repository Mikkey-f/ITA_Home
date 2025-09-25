package com.ita.home.service.impl;

import com.ita.home.mapper.UserOjMapper;
import com.ita.home.model.entity.UserOj;
import com.ita.home.model.vo.OjUserDataVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/25 15:40
 * 异步更新useroj服务
 */
@Service
@Slf4j
public class AsyncOjUpdateService {

    private final UserOjMapper userOjMapper;
    private final LocalLockService localLockService;


    @Autowired
    public AsyncOjUpdateService(UserOjMapper userOjMapper,
                                LocalLockService localLockService){
        this.userOjMapper = userOjMapper;
        this.localLockService = localLockService;
    }

    /**
     * 使用ojApiExecutorService线程池，异步更新用户OJ数据
     */
    @Async("ojApiExecutorService")
    public CompletableFuture<Boolean> updateUserOjDataAsync(OjUserDataVo ojDataVo, Long userId) {
        String lockKey = localLockService.getUpdateLockKey(userId);

        // 尝试获取锁，防止重复更新
        if (!localLockService.tryLock(lockKey)) {
            log.info("用户{}正在更新中，跳过本次更新", userId);
            return CompletableFuture.completedFuture(false);
        }
        try {
            return updateUserOjDataWithRetry(ojDataVo, userId);
        } finally {
            localLockService.releaseLock(lockKey);
        }
    }

    /**
     * 对数据库缓存进行带重试的更新逻辑
     */
    private CompletableFuture<Boolean> updateUserOjDataWithRetry(OjUserDataVo realTimeData, Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            for (int i = 0; i < 3; i++) {
                try {
                    // 1. 获取用户OJ配置
                    UserOj userOj = userOjMapper.findByUserId(userId);
                    if (userOj == null) {
                        log.warn("用户{}的OJ配置不存在", userId);
                        return false;
                    }
                    // 2. 更新数据库
                    return updateDatabase(userId, realTimeData);
                } catch (Exception e) {
                    log.error("用户{}数据更新失败，第{}次重试", userId, i + 1, e);
                    if (i < 3 - 1) {
                        try {
                            Thread.sleep(1000 * (i + 1)); // 指数退避
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            log.error("用户{}数据更新最终失败，已重试{}次", userId, 3);
            return false;
        });
    }

    /**
     * 更新数据库
     */
    private boolean updateDatabase(Long userId, OjUserDataVo data) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int result = userOjMapper.updateCacheData(
                    userId,
                    data.getTotalAc(),
                    data.getTotalSubmit(),
                    now,
                    now,
                    now
            );
            return result > 0;
        } catch (Exception e) {
            log.error("更新用户{}数据库失败", userId, e);
            return false;
        }
    }

    /**
     * 仅更新访问时间
     */
    @Async("ojApiExecutorService")
    public void updateLastAccessTimeAsync(Long userId) {
        try {
            userOjMapper.updateLastAccessTime(userId, LocalDateTime.now());
        } catch (Exception e) {
            log.error("更新用户{}访问时间失败", userId, e);
        }
    }

    private String getCacheKey(Long userId) {
        return "oj_data:" + userId;
    }
}
