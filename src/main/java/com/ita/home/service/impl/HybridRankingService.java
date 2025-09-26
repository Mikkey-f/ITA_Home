package com.ita.home.service.impl;

import com.ita.home.config.cache.CaffeineRankingCache;
import com.ita.home.enums.OjPlatformEnum;
import com.ita.home.mapper.UserOjMapper;
import com.ita.home.mapper.UserPlatformRankingMapper;
import com.ita.home.model.entity.UserOj;
import com.ita.home.model.entity.UserPlatformRanking;
import com.ita.home.model.vo.UserPlatformRankingVo;
import com.ita.home.service.impl.async.AsyncRankingService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/26 15:41
 * 排名混合查询策略：L1(2-5分钟查缓存) -> L2(5-10分钟查数据库缓存表) -> 实时数据(并激发异步更新策略)
 */
@Service
@Slf4j
public class HybridRankingService {

    private final UserPlatformRankingMapper rankingMapper;
    private final UserOjMapper userOjMapper;
    private final AsyncRankingService asyncRankingService;
    private final CaffeineRankingCache caffeineRankingCache;

    public HybridRankingService(CaffeineRankingCache caffeineRankingCache,
                                UserPlatformRankingMapper userPlatformRankingMapper,
                                UserOjMapper userOjMapper,
                                AsyncRankingService asyncRankingService) {
        this.caffeineRankingCache = caffeineRankingCache;
        this.rankingMapper = userPlatformRankingMapper;
        this.userOjMapper = userOjMapper;
        this.asyncRankingService = asyncRankingService;
    }
    /**
     * 获取用户平台排名 - 混合缓存策略
     */
    public UserPlatformRankingVo getUserPlatformRanking(String platformId, Long userId) {

        // L1: 先查Caffeine本地缓存
        Optional<UserPlatformRankingVo> cached = caffeineRankingCache.getRanking(platformId, userId);
        if (cached.isPresent()) {
            log.info("L1缓存命中 - 用户{}平台{}", userId, platformId);
            return cached.get();
        }

        // L2: 查询预计算的排名表
        UserPlatformRanking dbRanking = rankingMapper.findByUserIdAndPlatform(userId, platformId);
        if (dbRanking != null && isRankingCacheValid(dbRanking)) {
            UserPlatformRankingVo result = convertToVo(dbRanking);

            // 存入L1缓存
            caffeineRankingCache.putRanking(platformId, userId, result);
            log.info("L2缓存命中 - 用户{}平台{}", userId, platformId);
            return result;
        }

        // L3: 缓存都失效，实时计算排名
        log.info("缓存未命中，实时计算排名 - 用户{}平台{}", userId, platformId);
        UserPlatformRankingVo result = calculateRankingRealTime(platformId, userId);

        // 异步更新L2缓存
        asyncRankingService.updateSingleUserRankingAsync(platformId, userId, result);

        // 存入L1缓存
        caffeineRankingCache.putRanking(platformId, userId, result);

        return result;
    }

    /**
     * 刷新目标user_platform_ranking表的表项
     */
    public UserPlatformRankingVo refreshPlatformRanking(String platformId, Long userId) {
        // L2缓存，L1缓存更新
        UserPlatformRankingVo userPlatformRankingVo = calculateRankingRealTime(platformId, userId);
        asyncRankingService.updateSingleUserRankingAsync(platformId, userId, userPlatformRankingVo);
        caffeineRankingCache.putRanking(platformId, userId, userPlatformRankingVo);
        return userPlatformRankingVo;
    }

    private UserPlatformRankingVo convertToVo(UserPlatformRanking dbRanking) {
        return UserPlatformRankingVo.builder()
                .platformId(dbRanking.getPlatformId())
                .platformName(dbRanking.getPlatformName())
                .ranking(dbRanking.getRanking())
                .acCount(dbRanking.getAcCount())
                .rankingPercentage(dbRanking.getRankingPercentageAsDouble())
                .totalUsers(dbRanking.getTotalUsers())
                .submitCount(dbRanking.getSubmitCount())
                .username(dbRanking.getUsername())
                .build();
    }

    /**
     * 判断排名缓存是否有效
     * 策略：5分钟内的预计算结果认为有效
     */
    private boolean isRankingCacheValid(UserPlatformRanking ranking) {

        LocalDateTime expireTime = ranking.getLastCalcTime().plusMinutes(5);
        boolean valid = LocalDateTime.now().isBefore(expireTime);

        if (!valid) {
            log.debug("排名缓存已过期: {}, 过期时间: {}",
                    ranking.getLastCalcTime(), expireTime);
        }

        return valid;
    }

    /**
     * 实时计算排名（降级方案）
     */
    private UserPlatformRankingVo calculateRankingRealTime(String platformId, Long userId) {
        log.warn("执行实时排名计算，可能影响性能 - 用户{}平台{}", userId, platformId);

        // 获取用户数据
        UserOj userOj = userOjMapper.findByUserId(userId);
        if (userOj == null) {
            throw new RuntimeException("用户OJ数据不存在");
        }

        // 验证平台
        OjPlatformEnum platform = OjPlatformEnum.getByPlatformId(platformId);
        if (platform == null) {
            throw new RuntimeException("不支持的OJ平台: " + platformId);
        }

        // 提取平台数据
        PlatformData userData = extractUserPlatformData(userOj, platform);
        if (userData.getUsername() == null || userData.getUsername().trim().isEmpty()) {
            throw new RuntimeException("用户未配置" + platform.getPlatformName() + "账号");
        }

        // 计算排名（使用优化后的SQL）
        Integer ranking = userOjMapper.calculateUserRanking(
                platformId, userData.getAcCount(), userData.getSubmitCount());
        Integer totalUsers = userOjMapper.getTotalUsersWithPlatformData(platformId);

        // 计算排名百分比
        double percentage = totalUsers > 0 ? (double) ranking / totalUsers * 100 : 0.0;

        return UserPlatformRankingVo.builder()
                .platformId(platformId)
                .platformName(platform.getPlatformName())
                .ranking(ranking)
                .acCount(userData.getAcCount())
                .submitCount(userData.getSubmitCount())
                .totalUsers(totalUsers)
                .rankingPercentage(Math.round(percentage * 100.0) / 100.0)
                .username(userData.getUsername())
                .build();
    }
    /**
     * 提取用户在指定平台的数据
     */
    private HybridRankingService.PlatformData extractUserPlatformData(UserOj userOj, OjPlatformEnum platform) {
        return switch (platform) {
            case LUOGU -> new HybridRankingService.PlatformData(
                    userOj.getLuoguUsername(),
                    userOj.getLuoguAcNum() != null ? userOj.getLuoguAcNum() : 0,
                    userOj.getLuoguSubmitNum() != null ? userOj.getLuoguSubmitNum() : 0
            );
            case LEETCODE_CN -> new HybridRankingService.PlatformData(
                    userOj.getLeetcodeCnUsername(),
                    userOj.getLeetcodeAcNum() != null ? userOj.getLeetcodeAcNum() : 0,
                    userOj.getLeetcodeSubmitNum() != null ? userOj.getLeetcodeSubmitNum() : 0
            );
            case NOWCODER -> new HybridRankingService.PlatformData(
                    userOj.getNowcoderUserId(),
                    userOj.getNowcoderAcNum() != null ? userOj.getNowcoderAcNum() : 0,
                    userOj.getNowcoderSubmitNum() != null ? userOj.getNowcoderSubmitNum() : 0
            );
            case CODEFORCES -> new HybridRankingService.PlatformData(
                    userOj.getCodeforceUsername(),
                    userOj.getCodeforcesAcNum() != null ? userOj.getCodeforcesAcNum() : 0,
                    userOj.getCodeforcesSubmitNum() != null ? userOj.getCodeforcesSubmitNum() : 0
            );
            default -> throw new RuntimeException("未支持的平台类型");
        };
    }
    // 内部数据类
    @Data
    @AllArgsConstructor
    private static class PlatformData {
        private String username;
        private Integer acCount;
        private Integer submitCount;
    }

}
