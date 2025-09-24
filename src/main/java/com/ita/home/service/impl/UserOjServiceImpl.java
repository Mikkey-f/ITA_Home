package com.ita.home.service.impl;


import com.ita.home.mapper.UserOjMapper;
import com.ita.home.model.entity.UserOj;
import com.ita.home.service.UserOjService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;

/**
 * 用户OJ平台账号服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserOjServiceImpl implements UserOjService {

    private final UserOjMapper userOjMapper;
    private final RestTemplate restTemplate;

    /** OJHunt API的基础URL */
    @Value("${ita.oj.target}")
    private static String OJ_HUNT_API_BASE_URL;

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

}