package com.ita.home.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/25 15:38
 */
@Service
@Slf4j
public class LocalLockService {

    private final Cache<String, String> lockCache;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Autowired
    public LocalLockService(@Qualifier("updateLockCache") Cache<String, String> lockCache) {
        this.lockCache = lockCache;
    }

    /**
     * 尝试获取锁
     * @param lockKey 锁的key
     * @return 是否成功获取锁
     */
    public boolean tryLock(String lockKey) {
        // 获取读写锁，以下逻辑同一时间只能有一个线程进行
        lock.writeLock().lock();
        try {
            String existing = lockCache.getIfPresent(lockKey);
            if (existing == null) {
                lockCache.put(lockKey, Thread.currentThread().getName());
                log.debug("成功获取锁: {}", lockKey);
                return true;
            }
            log.debug("锁已被占用: {}, holder: {}", lockKey, existing);
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 释放锁
     */
    public void releaseLock(String lockKey) {
        lock.writeLock().lock();
        try {
            lockCache.invalidate(lockKey);
            log.debug("释放锁: {}", lockKey);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 生成更新锁的key
     */
    public String getUpdateLockKey(Long userId) {
        return "oj_update_lock:" + userId;
    }
}
