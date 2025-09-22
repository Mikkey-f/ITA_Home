package com.ita.home.service;

import com.ita.home.model.entity.User;
import com.ita.home.model.req.LoginRequest;
import com.ita.home.model.req.RegisterRequest;

/**
 * 用户服务接口
 * 定义用户相关的业务操作
 */
public interface UserService {

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求信息
     * @return 注册是否成功
     */
    boolean register(RegisterRequest registerRequest);

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求信息
     * @return 登录成功返回用户信息，失败返回null
     */
    User login(LoginRequest loginRequest);

    /**
     * 根据用户名查找用户
     * 
     * @param name 用户名
     * @return 用户信息
     */
    User findByName(String name);

    /**
     * 根据用户ID查找用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    User findById(Long id);

    /**
     * 检查用户名是否存在
     * 
     * @param name 用户名
     * @return 存在返回true，不存在返回false
     */
    boolean isNameExist(String name);
}