package com.ita.home.service.impl;

import com.ita.home.mapper.UserMapper;
import com.ita.home.model.entity.User;
import com.ita.home.model.req.LoginRequest;
import com.ita.home.model.req.RegisterRequest;
import com.ita.home.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 用户服务实现类
 * 实现用户相关的业务逻辑
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    // 密码加密工具
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     */
    @Override
    public boolean register(RegisterRequest registerRequest) {

        User user = User.builder()
                .name(registerRequest.getName())
                .password(passwordEncoder.encode(registerRequest.getPassword())) // 密码加密
                .avatar(ThreadLocalRandom.current().nextInt(1, 10))
                .build();

        int result = userMapper.insert(user);
        return result > 0;
    }

    /**
     * 用户登录
     */
    @Override
    public User login(LoginRequest loginRequest) {
        // 1. 参数验证
        if (!isValidLoginRequest(loginRequest)) {
            return null;
        }

        // 2. 根据用户名查找用户
        User user = findByName(loginRequest.getName());
        if (user == null) {
            return null; // 用户不存在
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return null; // 密码错误
        }


        user.setPassword("0"); // 清除密码信息
        return user;
    }

    /**
     * 根据用户名查找用户
     */
    @Override
    public User findByName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        return userMapper.findByName(name);
    }

    /**
     * 根据用户ID查找用户
     */
    @Override
    public User findById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return userMapper.selectById(id);
    }

    /**
     * 检查用户名是否存在
     */
    @Override
    public boolean isNameExist(String name) {
        if (!StringUtils.hasText(name)) {
            return false;
        }
        return userMapper.existsByName(name);
    }

    /**
     * 验证登录请求参数
     */
    private boolean isValidLoginRequest(LoginRequest request) {
        if (request == null) {
            return false;
        }
        return StringUtils.hasText(request.getName()) && StringUtils.hasText(request.getPassword());
    }
}