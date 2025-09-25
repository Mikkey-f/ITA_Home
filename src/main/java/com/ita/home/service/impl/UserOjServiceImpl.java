package com.ita.home.service.impl;


import com.github.benmanes.caffeine.cache.Cache;
import com.ita.home.exception.BaseException;
import com.ita.home.mapper.UserOjMapper;
import com.ita.home.model.dto.OjUserDataDto;
import com.ita.home.model.dto.UserRankingDto;
import com.ita.home.model.entity.UserOj;
import com.ita.home.model.req.RankingRequest;
import com.ita.home.model.vo.OjUserDataVo;
import com.ita.home.model.vo.RankingPageVo;
import com.ita.home.model.vo.UserRankingVo;
import com.ita.home.service.UserOjService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 用户OJ平台账号服务实现类
 */
@Service
@Slf4j
public class UserOjServiceImpl implements UserOjService {


    /** 线程池用于并行调用API */
    private final ExecutorService executorService;
    private final UserOjMapper userOjMapper;
    private final RestTemplate restTemplate;
    private final Cache<String, OjUserDataVo> ojDataCache;
    private final AsyncOjUpdateService asyncOjUpdateService;
    @Value("${ita.oj.cache.expire-hours}")
    private Integer expireHours;

    @Autowired
    public UserOjServiceImpl(@Qualifier("ojApiExecutorService") ExecutorService executorService,
                             UserOjMapper userOjMapper,
                             RestTemplate restTemplate,
                             @Qualifier("ojDataCache") Cache<String, OjUserDataVo> ojDataCache,
                             AsyncOjUpdateService asyncOjUpdateService) {
        this.executorService = executorService;
        this.userOjMapper = userOjMapper;
        this.restTemplate = restTemplate;
        this.ojDataCache = ojDataCache;
        this.asyncOjUpdateService = asyncOjUpdateService;
    }

    /** OJHunt API的基础URL */
    @Value("${ita.oj.target}")
    private String OJ_HUNT_API_BASE_URL;

    /**
     * 更新用户的oj账户
     */
    @Override
    @Transactional
    public boolean updateUserOjAccount(UserOj userOj) {
        try {
            userOj.setUpdateTime(LocalDateTime.now());
            int result = userOjMapper.updateById(userOj);
            log.info("更新用户OJ账号成功: id={}", userOj.getId());
            return result > 0;
        } catch (Exception e) {
            log.error("更新用户OJ账号失败: id={}", userOj.getId(), e);
            return false;
        }
    }

    /**
     * 用户注册时同时创建对应的oj表字段
     */
    @Override
    public boolean addUserOjAccountWithNull(Long userId) {
        UserOj userOj = UserOj.builder()
                .userId(userId)
                .build();
        int insert = userOjMapper.insert(userOj);
        return insert > 0;
    }

    /**
     * 返回userOj
     */
    @Override
    public UserOj getUserOjAccount(Long userId) {
        return userOjMapper.findByUserId(userId);
    }

    /**
     * 后续controller层获取oj信息均从这里获取
     */
    @Override
    public OjUserDataVo getCacheOjUserDataVo(Long userId) {
        long startTime = System.nanoTime();
        String cacheKey = "oj_data:" + userId;

        try {
            // 1. 先检查Caffeine缓存
            OjUserDataVo cachedData = ojDataCache.getIfPresent(cacheKey);
            if (cachedData != null) {
                log.info("用户{}命中Caffeine缓存", userId);
                // 异步更新访问时间
                asyncOjUpdateService.updateLastAccessTimeAsync(userId);
                return cachedData;
            }

            // 2. 检查数据库缓存
            UserOj userOj = userOjMapper.findByUserId(userId);
            if (userOj == null) {
                log.warn("用户{}的OJ账号信息不存在", userId);
                return OjUserDataVo.builder()
                        .ojUserDataDtoList(new ArrayList<>())
                        .totalAc(0)
                        .totalSubmit(0)
                        .build();
            }

            // 3. 判断数据库缓存是否有效
            if (isDatabaseCacheValid(userOj)) {
                log.info("用户{}命中数据库缓存", userId);
                // 构建VO并放入Caffeine缓存，不反会ac list
                OjUserDataVo dbCachedData = buildVoFromDatabase(userOj);
                ojDataCache.put(cacheKey, dbCachedData);

                // 异步更新访问时间
                asyncOjUpdateService.updateLastAccessTimeAsync(userId);
                return dbCachedData;
            }

            // 4. 缓存都无效，获取实时数据
            log.info("用户{}缓存失效，获取实时数据", userId);
            OjUserDataVo realTimeData = getRealTimeOjUserDataVo(userOj.getUserId());

            // 5. 放入Caffeine缓存
            realTimeData.setOjUserDataDtoList(new ArrayList<>());
            ojDataCache.put(cacheKey, realTimeData);

            // 6. 异步更新数据库
            asyncOjUpdateService.updateUserOjDataAsync(realTimeData, userOj.getUserId());

            return realTimeData;

        } catch (Exception e) {
            log.error("获取用户{}OJ数据失败", userId, e);
            return OjUserDataVo.builder()
                    .ojUserDataDtoList(new ArrayList<>())
                    .totalAc(0)
                    .totalSubmit(0)
                    .build();
        } finally {
            long endTime = System.nanoTime();
            log.info("用户{}获取OJ数据耗时: {}ms", userId, (endTime - startTime) / 1_000_000);
        }
    }

    /**
     * 获取排名页
     */
    @Override
    public RankingPageVo getUserRanking(RankingRequest request) {
        try {
            int offset = (request.getPageNum() - 1) * request.getPageSize();
            int limit = request.getPageSize();

            // 根据条件选择不同的查询方法
            List<UserRankingDto> rankingDtos;
            Long total;

            if (request.getOnlyActiveUsers()) {
                rankingDtos = userOjMapper.findActiveUserRankings(offset, limit);
                total = userOjMapper.countActiveUsers();
            } else {
                rankingDtos = userOjMapper.findAllUserRankings(offset, limit);
                total = userOjMapper.countAllUsers();
            }

            // 计算排名和构建VO
            List<UserRankingVo> rankings = new ArrayList<>();
            for (int i = 0; i < rankingDtos.size(); i++) {
                UserRankingDto dto = rankingDtos.get(i);
                int rank = offset + i + 1;

                double acRate = dto.getTotalSubmit() > 0
                        ? (dto.getTotalAc() * 100.0 / dto.getTotalSubmit()) : 0.0;

                rankings.add(UserRankingVo.builder()
                        .rank(rank)
                        .userId(dto.getUserId())
                        .username(dto.getName())
                        .totalAc(dto.getTotalAc())
                        .totalSubmit(dto.getTotalSubmit())
                        .acRate(Math.round(acRate * 100.0) / 100.0)
                        .lastUpdateTime(dto.getLastUpdateTime())
                        .build());
            }

            int totalPages = (int) Math.ceil((double) total / request.getPageSize());

            return RankingPageVo.builder()
                    .rankings(rankings)
                    .total(total)
                    .pageNum(request.getPageNum())
                    .pageSize(request.getPageSize())
                    .totalPages(totalPages)
                    .hasNext(request.getPageNum() < totalPages)
                    .hasPrevious(request.getPageNum() > 1)
                    .build();

        } catch (Exception e) {
            log.error("获取用户排名失败", e);
            throw new BaseException("获取用户排名失败");
        }
    }

    /**
     * 获取具体用户排名
     */
    @Override
    public UserRankingVo getUserRankById(Long userId) {
        try {
            UserRankingDto rankingDto = userOjMapper.findUserDataById(userId);
            if (rankingDto == null) {
                return null;
            }
            // 计算AC率
            double acRate = rankingDto.getTotalSubmit() != null && rankingDto.getTotalSubmit() > 0
                    ? (rankingDto.getTotalAc() * 100.0 / rankingDto.getTotalSubmit()) : 0.0;

            // 计算排名
            Integer rank = userOjMapper.countBetterUsers(rankingDto.getTotalAc(), rankingDto.getTotalSubmit()) + 1;

            return UserRankingVo.builder()
                    .rank(rank)
                    .userId(rankingDto.getUserId())
                    .username(rankingDto.getName())
                    .totalAc(rankingDto.getTotalAc())
                    .totalSubmit(rankingDto.getTotalSubmit())
                    .acRate(Math.round(acRate * 100.0) / 100.0)
                    .lastUpdateTime(rankingDto.getLastUpdateTime())
                    .build();
        } catch (Exception e) {
            log.error("获取用户{}排名失败", userId, e);
            throw new BaseException("获取用户排名失败");
        }
    }

    /**
     * 绕过了缓存，仅限内部特殊业务调用外部禁止调用，直接获取实时Oj信息
     */
    @Override
    public OjUserDataVo getRealTimeOjUserDataVo(Long userId) {
        // 记录方法开始时间
        long startTime = System.nanoTime();
        
        try {
            // 从数据库获取用户OJ账号信息
            UserOj userOj = userOjMapper.findByUserId(userId);
            if (userOj == null) {
                log.warn("用户{}的OJ账号信息不存在", userId);
                return OjUserDataVo.builder()
                        .ojUserDataDtoList(new ArrayList<>())
                        .totalAc(0)
                        .totalSubmit(0)
                        .build();
            }

            // 定义平台信息：平台代码 -> 用户名
            Map<String, String> platformUserMap = getPlatformValue(userOj);
            
            if (platformUserMap.isEmpty()) {
                log.warn("用户{}没有配置任何OJ平台账号", userId);

                return OjUserDataVo.builder()
                        .ojUserDataDtoList(new ArrayList<>())
                        .totalAc(0)
                        .totalSubmit(0)
                        .build();
            }

            // 创建并行任务列表
            List<CompletableFuture<OjUserDataDto>> futures = new ArrayList<>();
            
            // 为每个平台创建异步任务
            for (Map.Entry<String, String> entry : platformUserMap.entrySet()) {
                String platformCode = entry.getKey();
                String username = entry.getValue();
                
                CompletableFuture<OjUserDataDto> future = CompletableFuture.supplyAsync(() -> {
                    return fetchSinglePlatformData(platformCode, username);
                }, executorService)
                .orTimeout(10, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    log.error("平台{}数据获取失败（超时或异常），用户名：{}", platformCode, username, throwable);
                    return null; // 超时/异常时返回null，不影响其他任务
                });
                
                futures.add(future);
            }

            // 等待所有任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            // 获取所有结果并汇总
            List<OjUserDataDto> ojUserDataDtoList = new ArrayList<>();
            int totalAc = 0;
            int totalSubmit = 0;

            allFutures.join(); // 等待所有任务完成

            for (CompletableFuture<OjUserDataDto> future : futures) {
                try {
                    OjUserDataDto result = future.get();
                    if (result != null && Boolean.FALSE.equals(result.getError()) && result.getData() != null) {
                        ojUserDataDtoList.add(result);
                        
                        OjUserDataDto.UserData data = result.getData();
                        if (data.getSolved() != null) {
                            totalAc += data.getSolved();
                        }
                        if (data.getSubmissions() != null) {
                            totalSubmit += data.getSubmissions();
                        }
                    }
                } catch (Exception e) {
                    log.error("获取异步任务结果失败", e);
                }
            }
            long endTime = System.nanoTime();
            log.info("{}ns", endTime - startTime);
            // 构建返回结果
            return OjUserDataVo.builder()
                    .ojUserDataDtoList(ojUserDataDtoList)
                    .totalAc(totalAc)
                    .totalSubmit(totalSubmit)
                    .build();

        } catch (Exception e) {
            log.error("获取用户{}OJ数据汇总失败", userId, e);
            return OjUserDataVo.builder()
                    .ojUserDataDtoList(new ArrayList<>())
                    .totalAc(0)
                    .totalSubmit(0)
                    .build();
        }
    }

    /**
     * 获取单个平台的数据（异步调用）
     */
    private OjUserDataDto fetchSinglePlatformData(String platformCode, String username) {
        try {
            // 构建API URL
            String apiUrl = String.format("%s/%s/%s", OJ_HUNT_API_BASE_URL, platformCode, username);
            log.info("并行调用OJHunt API: {} - {}", platformCode, apiUrl);

            // 调用外部API
            OjUserDataDto response = restTemplate.getForObject(apiUrl, OjUserDataDto.class);

            if (response != null && Boolean.FALSE.equals(response.getError()) && response.getData() != null) {
                OjUserDataDto.UserData data = response.getData();
                log.info("成功获取{}平台数据: user={}, solved={}, submissions={}",
                        platformCode, username, data.getSolved(), data.getSubmissions());
                return response;
            } else {
                log.warn("{}平台API返回错误或无数据: user={}", platformCode, username);
                return null;
            }

        } catch (Exception e) {
            log.error("调用{}平台API失败: user={}", platformCode, username, e);
            return null;
        }
    }

    private static Map<String, String> getPlatformValue(UserOj userOj) {
        Map<String, String> platformUserMap = new HashMap<>();

        // 检查各平台用户名，如果不为null且不为空则加入map
        if (userOj.getLuoguUsername() != null && !userOj.getLuoguUsername().trim().isEmpty()) {
            platformUserMap.put("luogu", userOj.getLuoguUsername());
        }
        if (userOj.getLeetcodeCnUsername() != null && !userOj.getLeetcodeCnUsername().trim().isEmpty()) {
            platformUserMap.put("leetcode_cn", userOj.getLeetcodeCnUsername());
        }
        if (userOj.getCodeforceUsername() != null && !userOj.getCodeforceUsername().trim().isEmpty()) {
            platformUserMap.put("codeforces", userOj.getCodeforceUsername());
        }
        if (userOj.getNowcoderUserId() != null && !userOj.getNowcoderUserId().trim().isEmpty()) {
            platformUserMap.put("nowcoder", userOj.getNowcoderUserId());
        }
        return platformUserMap;
    }


    /**
     * 应用关闭时清理线程池资源
     */
    @PreDestroy
    public void cleanup() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            log.info("OJ数据获取线程池已关闭");
        }
    }

    /**
     * 判断数据库缓存是否有效
     */
    private boolean isDatabaseCacheValid(UserOj userOj) {
        if (userOj.getCacheTime() == null ||
                userOj.getTotalAcNum() == null ||
                userOj.getTotalCommitNum() == null) {
            return false;
        }

        LocalDateTime expireTime = userOj.getCacheTime()
                .plusHours(expireHours);
        return LocalDateTime.now().isBefore(expireTime);
    }

    /**
     * 从数据库缓存构建VO
     */
    private OjUserDataVo buildVoFromDatabase(UserOj userOj) {
        return OjUserDataVo.builder()
                .ojUserDataDtoList(new ArrayList<>()) // 如果需要详细信息，需要额外存储
                .totalAc(userOj.getTotalAcNum())
                .totalSubmit(userOj.getTotalCommitNum())
                .build();
    }
}