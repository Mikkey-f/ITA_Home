package com.ita.home.service;

import com.ita.home.model.dto.OjUserDataDto;
import com.ita.home.model.entity.UserOj;
import com.ita.home.model.vo.OjUserDataVo;

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

    /**
     * 获取用户的userOj
     * @param userId 用户id
     * @return 返回userOj对象
     */
    UserOj getUserOjAccount(Long userId);

    /**
     * 绕过了缓存，仅限内部特殊业务调用外部禁止调用，直接获取实时Oj信息
     * @param userId 用户id
     * @return 返回ojUserDataVo对象
     */
    OjUserDataVo getRealTimeOjUserDataVo(Long userId);

    /**
     * 从caffeine缓存->数据库缓存->数据库如果无效，获取实时数据 -> 再写回数据库和caffeine
     * @param userId 用户id
     * @return 返回OjUserDataVo对象
     */
    OjUserDataVo getCacheOjUserDataVo(Long userId);

}