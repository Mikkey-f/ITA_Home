package com.ita.home.schedule;

import com.github.benmanes.caffeine.cache.Cache;
import com.ita.home.mapper.UserOjMapper;
import com.ita.home.model.entity.UserOj;
import com.ita.home.model.vo.OjUserDataVo;
import com.ita.home.service.UserOjService;
import com.ita.home.service.impl.AsyncOjUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/25 17:48
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "ita.cache.oj.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class OjDataSchedule {

    private final UserOjMapper userOjMapper;
    private final AsyncOjUpdateService asyncOjUpdateService;
    private final UserOjService userOjService;
    private final ExecutorService executorService;
    private final Cache<String, OjUserDataVo> ojDataCache;
    @Value("${ita.oj.cache.active-user-days}")
    private Integer activateUserDays;
    @Value("${ita.oj.schedule.batch-size}")
    private Integer batchSize;

    @Autowired
    public OjDataSchedule(UserOjMapper userOjMapper,
                          AsyncOjUpdateService asyncOjUpdateService,
                          UserOjService userOjService,
                          @Qualifier("ojApiExecutorService") ExecutorService executorService,
                          @Qualifier("ojDataCache") Cache<String, OjUserDataVo> ojDataCache) {
        this.userOjMapper = userOjMapper;
        this.asyncOjUpdateService = asyncOjUpdateService;
        this.userOjService = userOjService;
        this.executorService = executorService;
        this.ojDataCache = ojDataCache;
    }

    /**
     * 每天凌晨2点刷新活跃用户数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshActiveUsersData() {
        log.info("开始刷新活跃用户OJ数据");

        try {
            LocalDateTime activeTime = LocalDateTime.now()
                    .minusDays(activateUserDays);

            List<Long> activeUserIds = userOjMapper.findActiveUserIds(activeTime);
            log.info("找到{}个活跃用户需要刷新", activeUserIds.size());

            // 分批处理
            List<List<Long>> batches = partition(activeUserIds, batchSize);

            for (int i = 0; i < batches.size(); i++) {
                List<Long> batch = batches.get(i);
                log.info("处理第{}/{}批，用户数: {}", i + 1, batches.size(), batch.size());

                List<CompletableFuture<Boolean>> futures = batch.stream()
                        .map(userId -> CompletableFuture.supplyAsync(() -> {
                            try {
                                // 获得用户缓存key
                                String cacheKey = "oj_data:" + userId;
                                // 获取用户OJ配置
                                UserOj userOj = userOjMapper.findByUserId(userId);
                                if (userOj == null) return false;
                                // 获取实时数据
                                OjUserDataVo realTimeData = userOjService.getRealTimeOjUserDataVo(userId);
                                // 更新缓存
                                ojDataCache.put(cacheKey, realTimeData);
                                // 调用异步更新
                                return asyncOjUpdateService.updateUserOjDataAsync(realTimeData, userId).get();
                            } catch (Exception e) {
                                log.error("定时任务更新用户{}数据失败", userId, e);
                                return false;
                            }
                        }, executorService))
                        .toList();

                // 等待当前批次完成
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                // 批次间暂停，避免对系统造成太大压力
                if (i < batches.size() - 1) {
                    try {
                        Thread.sleep(5000); // 5秒间隔
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            log.info("活跃用户OJ数据刷新完成");

        } catch (Exception e) {
            log.error("刷新活跃用户OJ数据失败", e);
        }
    }

    /**
     * 每周日凌晨3点清理不活跃用户缓存
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void cleanInactiveUsersCache() {
        log.info("开始清理不活跃用户缓存");

        try {
            LocalDateTime inactiveTime = LocalDateTime.now().minusDays(30);
            int cleanedCount = userOjMapper.clearInactiveUsersCache(inactiveTime);
            log.info("清理了{}个不活跃用户的缓存数据", cleanedCount);
        } catch (Exception e) {
            log.error("清理不活跃用户缓存失败", e);
        }
    }

    /**
     * 分批工具方法
     */
    private <T> List<List<T>> partition(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }
}
