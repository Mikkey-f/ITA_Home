package com.ita.home.schedule;

import com.github.benmanes.caffeine.cache.Cache;
import com.ita.home.enums.OjPlatformEnum;
import com.ita.home.mapper.UserOjMapper;
import com.ita.home.mapper.UserPlatformRankingMapper;
import com.ita.home.model.dto.PlatformUserDataDto;
import com.ita.home.model.entity.UserPlatformRanking;
import com.ita.home.model.vo.UserPlatformRankingVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 定时更新排名缓存表
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/26 11:30
 */
@Component
@Slf4j
public class RankingCalculationScheduler {

    private final UserOjMapper userOjMapper;
    private final UserPlatformRankingMapper rankingMapper;
    private final Cache<String, UserPlatformRankingVo> platformRankingCache;

    @Autowired
    RankingCalculationScheduler(UserOjMapper userOjMapper,
                                UserPlatformRankingMapper rankingMapper,
                                @Qualifier("platformRankingCache") Cache<String, UserPlatformRankingVo> platformRankingCache) {
        this.userOjMapper = userOjMapper;
        this.rankingMapper = rankingMapper;
        this.platformRankingCache = platformRankingCache;
    }

    /**
     * 核心定时任务：每10分钟重新计算所有平台排名
     */
    @Scheduled(fixedRate = 600000) // 1分钟
    public void calculateAllRankings() {
        log.info("开始计算所有平台排名...");

        for (OjPlatformEnum platform : OjPlatformEnum.values()) {
            try {
                long startTime = System.currentTimeMillis();
                calculateSinglePlatformRanking(platform);
                log.info("平台 {} 排名计算完成，耗时: {}ms",
                        platform.getPlatformName(),
                        System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                log.error("计算平台 {} 排名失败", platform.getPlatformName(), e);
            }
        }

        // 清空Caffeine缓存，强制下次查询从数据库读取最新排名
        platformRankingCache.invalidateAll();
        log.info("所有平台排名计算完成，Caffeine缓存已清空");
    }

    /**
     * 计算单个平台的排名
     */
    private void calculateSinglePlatformRanking(OjPlatformEnum platform) {
        String platformId = platform.getPlatformId();

        // 1. 获取该平台所有有效用户，按排名规则排序
        List<PlatformUserDataDto> users = getUserDataOrderedByRanking(platformId);
        if (users.isEmpty()) {
            log.warn("平台 {} 没有有效用户数据", platform.getPlatformName());
            return;
        }

        // 2. 计算排名（处理并列排名的情况）
        List<UserPlatformRanking> rankings = calculateRankingsWithTies(users, platform);

        // 3. 批量更新数据库（使用ON DUPLICATE KEY UPDATE）
        int batchSize = 500;
        for (int i = 0; i < rankings.size(); i += batchSize) {
            int end = Math.min(i + batchSize, rankings.size());
            List<UserPlatformRanking> batch = rankings.subList(i, end);
            rankingMapper.batchUpsertRankings(batch);
        }

        log.info("平台 {} 排名更新完成，共更新 {} 个用户", platform.getPlatformName(), rankings.size());
    }

    /**
     * 获取平台用户数据并排序
     */
    private List<PlatformUserDataDto> getUserDataOrderedByRanking(String platformId) {
        // 根据平台ID动态构建查询
        return switch (platformId) {
            case "luogu" -> userOjMapper.getLuoguUsersOrdered();
            case "leetcode" -> userOjMapper.getLeetcodeUsersOrdered();
            case "nowcoder" -> userOjMapper.getNowcoderUsersOrdered();
            case "codeforces" -> userOjMapper.getCodeforcesUsersOrdered();
            default -> new ArrayList<>();
        };
    }

    /**
     * 计算排名，正确处理并列排名
     */
    private List<UserPlatformRanking> calculateRankingsWithTies(
            List<PlatformUserDataDto> users, OjPlatformEnum platform) {

        List<UserPlatformRanking> rankings = new ArrayList<>();
        int currentRank = 1;
        Integer lastAc = null;
        Integer lastSubmit = null;
        LocalDateTime calcTime = LocalDateTime.now();

        for (int i = 0; i < users.size(); i++) {
            PlatformUserDataDto user = users.get(i);

            // 如果AC数或提交数发生变化，更新排名
            if (lastAc != null &&
                (!Objects.equals(lastAc, user.getAcCount()) ||
                 !Objects.equals(lastSubmit, user.getSubmitCount()))) {
                currentRank = i + 1; // 跳跃排名，比如：1, 1, 3, 4
            }

            // 计算排名百分比
            double percentage = (double) currentRank / users.size() * 100;

            rankings.add(UserPlatformRanking.builder()
                    .userId(user.getUserId())
                    .platformId(platform.getPlatformId())
                    .platformName(platform.getPlatformName())
                    .username(user.getUsername())
                    .ranking(currentRank)
                    .acCount(user.getAcCount())
                    .submitCount(user.getSubmitCount())
                    .totalUsers(users.size())
                    .rankingPercentage(BigDecimal.valueOf(percentage)
                            .setScale(2, RoundingMode.HALF_UP))
                    .lastCalcTime(calcTime)
                    .build());

            lastAc = user.getAcCount();
            lastSubmit = user.getSubmitCount();
        }

        return rankings;
    }
}
