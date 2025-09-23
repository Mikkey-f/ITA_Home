package com.ita.home.service;

import com.ita.home.model.entity.User;
import com.ita.home.model.req.LoginByEmailRequest;
import com.ita.home.model.req.LoginByNameRequest;
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
     * 用户使用用户名登录
     * 
     * @param loginRequest 登录请求信息
     * @return 登录成功返回用户信息，失败返回null
     */
    User loginByName(LoginByNameRequest loginRequest);

    /**
     * 用户使用邮箱登录
     *
     * @param loginRequest 登录请求信息
     * @return 登录成功返回用户信息，失败返回null
     */
    User loginByEmail(LoginByEmailRequest loginRequest);

    /**
     * 根据用户名查找用户
     * 
     * @param name 用户名
     * @return 用户信息
     */
    User findByName(String name);

    /**
     * 根据邮箱查找用户
     *
     * @param email 用户邮箱
     * @return 用户信息
     */
    User findByEmail(String email);

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

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 存在返回true，不存在返回false
     */
    boolean isEmailExist(String email);

    /**
     * 更新头像
     *
     * @param userId 用户id
     * @param avatar 用户头像
     * @return 存在返回true，不存在返回false
     */
    boolean updateUserAvatar(Long userId, Integer avatar);

    /**
     * 更新用户密码
     *
     * @param userId 用户id
     * @param oldPassword 用户旧密码
     * @param newPassword 用户新密码
     * @return 更新成果返回true，失败false
     */
    boolean updateUserPassword(Long userId, String oldPassword, String newPassword);


}