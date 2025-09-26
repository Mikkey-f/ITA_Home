package com.ita.home.service;

import com.ita.home.model.dto.OjUserDataDto;
import com.ita.home.model.entity.UserOj;
import com.ita.home.model.req.RankingRequest;
import com.ita.home.model.vo.OjUserDataVo;
import com.ita.home.model.vo.RankingPageVo;
import com.ita.home.model.vo.UserPlatformRankingVo;
import com.ita.home.model.vo.UserRankingVo;

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

    /**
     * 获取用户刷题排名（分页）
     * @param request 排名查询请求
     * @return 排名分页结果
     */
    RankingPageVo getUserRanking(RankingRequest request);

    /**
     * 获取指定用户的排名信息
     * @param userId 用户ID
     * @return 用户排名信息
     */
    UserRankingVo getUserRankById(Long userId);

    /**
     * 获取用户在指定平台的排名信息
     * @param platformId 平台ID
     * @param userId 用户ID
     * @return 用户平台排名信息
     */
    UserPlatformRankingVo getUserPlatformRanking(String platformId, Long userId);

    /**
     * 根据UserOj表里面刷新指定平台的缓存表和缓存
     * @param platformId 平台ID
     * @param userId 用户ID
     * @return 用户平台排名信息
     */
    UserPlatformRankingVo refreshPlatformRanking(String platformId, Long userId);
}