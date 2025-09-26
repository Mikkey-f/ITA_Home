package com.ita.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ita.home.model.entity.UserPlatformRanking;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/26 14:42
 */
@Mapper
public interface UserPlatformRankingMapper extends BaseMapper<UserPlatformRanking> {
    /**
     * 批量插入或更新用户平台排名
     * 使用ON DUPLICATE KEY UPDATE处理重复数据
     */
    @Insert({
            "<script>",
            "INSERT INTO ita_home.user_platform_ranking (",
            "    user_id, platform_id, platform_name, username, ranking,",
            "    ac_count, submit_count, total_users, ranking_percentage, last_calc_time",
            ") VALUES ",
            "<foreach collection='rankings' item='item' separator=','>",
            "    (",
            "        #{item.userId},",
            "        #{item.platformId},",
            "        #{item.platformName},",
            "        #{item.username},",
            "        #{item.ranking},",
            "        #{item.acCount},",
            "        #{item.submitCount},",
            "        #{item.totalUsers},",
            "        #{item.rankingPercentage},",
            "        #{item.lastCalcTime}",
            "    )",
            "</foreach>",
            "ON DUPLICATE KEY UPDATE",
            "    platform_name = VALUES(platform_name),",
            "    username = VALUES(username),",
            "    ranking = VALUES(ranking),",
            "    ac_count = VALUES(ac_count),",
            "    submit_count = VALUES(submit_count),",
            "    total_users = VALUES(total_users),",
            "    ranking_percentage = VALUES(ranking_percentage),",
            "    last_calc_time = VALUES(last_calc_time)",  // MySQL自动更新update_time
            "</script>"
    })
    int batchUpsertRankings(@Param("rankings") List<UserPlatformRanking> rankings);

    /**
     * 插入或更新单个用户的平台排名记录
     * 基于(user_id, platform_id)唯一约束进行upsert操作
     */
    @Insert("INSERT INTO ita_home.user_platform_ranking (" +
            "    user_id, platform_id, platform_name, username, ranking," +
            "    ac_count, submit_count, total_users, ranking_percentage, last_calc_time" +
            ") VALUES (" +
            "    #{userId}, #{platformId}, #{platformName}, #{username}, #{ranking}," +
            "    #{acCount}, #{submitCount}, #{totalUsers}, #{rankingPercentage}, #{lastCalcTime}" +
            ") ON DUPLICATE KEY UPDATE" +
            "    platform_name = VALUES(platform_name)," +
            "    username = VALUES(username)," +
            "    ranking = VALUES(ranking)," +
            "    ac_count = VALUES(ac_count)," +
            "    submit_count = VALUES(submit_count)," +
            "    total_users = VALUES(total_users)," +
            "    ranking_percentage = VALUES(ranking_percentage)," +
            "    last_calc_time = VALUES(last_calc_time)," +
            "    update_time = CURRENT_TIMESTAMP")
    int upsertSingleRanking(UserPlatformRanking ranking);

    /**
     * 根据用户ID和平台ID查询排名信息
     * 基于唯一索引(user_id, platform_id)进行精确查询
     */
    @Select("SELECT * FROM ita_home.user_platform_ranking " +
            "WHERE user_id = #{userId} AND platform_id = #{platformId}")
    UserPlatformRanking findByUserIdAndPlatform(@Param("userId") Long userId,
                                                @Param("platformId") String platformId);
}
