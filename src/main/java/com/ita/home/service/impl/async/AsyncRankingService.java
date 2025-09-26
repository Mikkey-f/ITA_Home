package com.ita.home.service.impl.async;

import com.ita.home.mapper.UserPlatformRankingMapper;
import com.ita.home.model.entity.UserPlatformRanking;
import com.ita.home.model.vo.UserPlatformRankingVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/26 15:51
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncRankingService {

    private final UserPlatformRankingMapper rankingMapper;
    /**
     * 异步更新单个用户的排名缓存
     */
    @Async("rankingExecutorService")
    public CompletableFuture<Void> updateSingleUserRankingAsync(
            String platformId, Long userId, UserPlatformRankingVo rankingVo) {

        try {
            UserPlatformRanking ranking = UserPlatformRanking.builder()
                    .userId(userId)
                    .platformId(platformId)
                    .platformName(rankingVo.getPlatformName())
                    .username(rankingVo.getUsername())
                    .ranking(rankingVo.getRanking())
                    .acCount(rankingVo.getAcCount())
                    .submitCount(rankingVo.getSubmitCount())
                    .totalUsers(rankingVo.getTotalUsers())
                    .rankingPercentage(BigDecimal.valueOf(rankingVo.getRankingPercentage()))
                    .lastCalcTime(LocalDateTime.now())
                    .build();

            rankingMapper.upsertSingleRanking(ranking);
            log.debug("异步更新用户{}平台{}排名成功", userId, platformId);

        } catch (Exception e) {
            log.error("异步更新用户{}平台{}排名失败", userId, platformId, e);
        }

        return CompletableFuture.completedFuture(null);
    }
}
