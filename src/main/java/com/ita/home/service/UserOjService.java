package com.ita.home.service;

import com.ita.home.model.dto.OjUserDataDto;
import com.ita.home.model.entity.UserOj;

import java.util.List;

/**
 * 用户OJ平台账号服务接口
 * 定义OJ平台相关的业务操作
 */
public interface UserOjService {

    /**
     * 更新用户OJ账号
     *
     * @param userOj 用户OJ账号信息
     * @return 更新是否成功
     */
    boolean updateUserOjAccount(UserOj userOj);

    /**
     * 创建空UserOj行
     * @param userId 用户id
     * @return 返回插入是否成功
     */
    boolean addUserOjAccountWithNull(Long userId);

}