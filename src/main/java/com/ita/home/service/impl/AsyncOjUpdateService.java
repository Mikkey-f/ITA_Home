package com.ita.home.service.impl;

import com.ita.home.mapper.UserOjMapper;
import com.ita.home.model.dto.OjDataDto;
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
     * 使用ojApiExecutorService线程池，异步更新用户数据库OJ数据
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

            // 从ojDataDtoList中提取各个平台的数据
            int luoguAcNum = 0, luoguSubmitNum = 0;
            int leetcodeAcNum = 0, leetcodeSubmitNum = 0;
            int nowcoderAcNum = 0, nowcoderSubmitNum = 0;
            int codeforceAcNum = 0, codeforceSubmitNum = 0;

            if (data.getOjDataDtoList() != null) {
                for (OjDataDto ojData : data.getOjDataDtoList()) {
                    String platformName = ojData.getName();
                    if (platformName == null) continue;

                    switch (platformName.toLowerCase()) {
                        case "luogu":
                        case "洛谷":
                            luoguAcNum = ojData.getSolved() != null ? ojData.getSolved() : 0;
                            luoguSubmitNum = ojData.getSubmitted() != null ? ojData.getSubmitted() : 0;
                            break;
                        case "leetcode":
                        case "leetcode-cn":
                        case "力扣":
                            leetcodeAcNum = ojData.getSolved() != null ? ojData.getSolved() : 0;
                            leetcodeSubmitNum = ojData.getSubmitted() != null ? ojData.getSubmitted() : 0;
                            break;
                        case "nowcoder":
                        case "牛客":
                        case "牛客网":
                            nowcoderAcNum = ojData.getSolved() != null ? ojData.getSolved() : 0;
                            nowcoderSubmitNum = ojData.getSubmitted() != null ? ojData.getSubmitted() : 0;
                            break;
                        case "codeforces":
                        case "cf":
                            codeforceAcNum = ojData.getSolved() != null ? ojData.getSolved() : 0;
                            codeforceSubmitNum = ojData.getSubmitted() != null ? ojData.getSubmitted() : 0;
                            break;
                        default:
                            log.warn("未知的OJ平台: {}", platformName);
                            break;
                    }
                }
            }

            int result = userOjMapper.updateCacheData(
                    userId,
                    data.getTotalAc(),
                    data.getTotalSubmit(),
                    luoguAcNum,
                    luoguSubmitNum,
                    leetcodeAcNum,
                    leetcodeSubmitNum,
                    nowcoderAcNum,
                    nowcoderSubmitNum,
                    codeforceAcNum,
                    codeforceSubmitNum,
                    now,  // cacheTime
                    now,  // lastAccessTime
                    now   // updateTime
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
