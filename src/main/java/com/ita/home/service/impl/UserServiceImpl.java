package com.ita.home.service.impl;

import com.ita.home.mapper.UserMapper;
import com.ita.home.model.entity.User;
import com.ita.home.model.req.LoginByEmailRequest;
import com.ita.home.model.req.LoginByNameRequest;
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
                .email(registerRequest.getEmail())
                .build();

        int result = userMapper.insert(user);
        return result > 0;
    }

    /**
     * 用户使用用户名登录
     */
    @Override
    public User loginByName(LoginByNameRequest loginRequest) {
        // 1. 参数验证
        if (!isValidLoginRequestForName(loginRequest)) {
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
     * 用户使用邮箱登录
     */
    @Override
    public User loginByEmail(LoginByEmailRequest loginRequest) {
        // 1. 参数验证
        if (!isValidLoginRequestForEmail(loginRequest)) {
            return null;
        }

        // 2. 根据用户名查找用户
        User user = findByEmail(loginRequest.getEmail());
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
     * 根据邮箱查找用户
     */
    @Override
    public User findByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return userMapper.findByEmail(email);
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
     * 检查邮箱是否存在
     */
    @Override
    public boolean isEmailExist(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return userMapper.existsByEmail(email);
    }

    /**
     * 更新用户头像
     */
    @Override
    public boolean updateUserAvatar(Long userId, Integer avatar) {
        if (!StringUtils.hasText(String.valueOf(avatar)) || !StringUtils.hasText(String.valueOf(userId))) {
            return false;
        }
        User user = findById(userId);
        user.setAvatar(avatar);
        int result = userMapper.updateById(user);
        return result > 0;
    }

    /**
     * 用户更新密码
     */
    @Override
    public boolean updateUserPassword(Long userId, String oldPassword, String newPassword) {
        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword) || !StringUtils.hasText(String.valueOf(userId))) {
            return false;
        }
        User user = findById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) return false;
        user.setPassword(passwordEncoder.encode(newPassword));
        int result = userMapper.updateById(user);
        return result > 0;
    }

    /**
     * 验证登录请求参数
     */
    private boolean isValidLoginRequestForName(LoginByNameRequest request) {
        if (request == null) {
            return false;
        }
        return StringUtils.hasText(request.getName()) && StringUtils.hasText(request.getPassword());
    }

    /**
     * 验证登录请求参数
     */
    private boolean isValidLoginRequestForEmail(LoginByEmailRequest request) {
        if (request == null) {
            return false;
        }
        return StringUtils.hasText(request.getEmail()) && StringUtils.hasText(request.getPassword());
    }
}