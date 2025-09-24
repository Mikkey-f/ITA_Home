package com.ita.home.service.impl;


import com.ita.home.mapper.UserOjMapper;
import com.ita.home.model.dto.OjUserDataDto;
import com.ita.home.model.entity.UserOj;
import com.ita.home.model.vo.OjUserDataVo;
import com.ita.home.service.UserOjService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.Executors;

/**
 * 用户OJ平台账号服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserOjServiceImpl implements UserOjService {

    private final UserOjMapper userOjMapper;
    private final RestTemplate restTemplate;

    /** 线程池用于并行调用API */
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

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

    @Override
    public OjUserDataVo getOjUserDataVo(Long userId) {
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
                }, executorService);
                
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
            return null;
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
}