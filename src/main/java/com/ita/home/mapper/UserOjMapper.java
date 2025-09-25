package com.ita.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ita.home.model.dto.UserRankingDto;
import com.ita.home.model.entity.User;
import com.ita.home.model.entity.UserOj;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户OJ平台账号Mapper接口
 * 继承MyBatis Plus的BaseMapper，自动拥有基本的CRUD方法
 */
@Mapper
public interface UserOjMapper extends BaseMapper<UserOj> {

    /**
     * 通过userId查找UserOj
     * @param userId 用户id输入
     * @return 返回UserOj
     */
    @Select("SELECT * FROM ita_home.user_oj WHERE user_id = #{userId}")
    UserOj findByUserId(Long userId);

    /**
     * 更新缓存数据
     */
    @Update("UPDATE ita_home.user_oj SET total_ac_num = #{totalAcNum}, " +
            "total_commit_num = #{totalCommitNum}, " +
            "cache_time = #{cacheTime}, " +
            "last_access_time = #{lastAccessTime}, " +
            "update_time = #{updateTime} " +
            "WHERE user_id = #{userId}")
    int updateCacheData(@Param("userId") Long userId,
                        @Param("totalAcNum") Integer totalAcNum,
                        @Param("totalCommitNum") Integer totalCommitNum,
                        @Param("cacheTime") LocalDateTime cacheTime,
                        @Param("lastAccessTime") LocalDateTime lastAccessTime,
                        @Param("updateTime") LocalDateTime updateTime);

    /**
     * 仅更新访问时间
     */
    @Update("UPDATE ita_home.user_oj SET last_access_time = #{lastAccessTime} WHERE user_id = #{userId}")
    void updateLastAccessTime(@Param("userId") Long userId,
                             @Param("lastAccessTime") LocalDateTime lastAccessTime);

    /**
     * 查询活跃用户ID
     */
    @Select("SELECT user_id FROM ita_home.user_oj WHERE last_access_time >= #{activeTime}")
    List<Long> findActiveUserIds(@Param("activeTime") LocalDateTime activeTime);

    /**
     * 清理不活跃用户的缓存
     */
    @Update("UPDATE ita_home.user_oj SET total_ac_num = NULL, total_commit_num = NULL, cache_time = NULL " +
            "WHERE last_access_time < #{inactiveTime}")
    int clearInactiveUsersCache(@Param("inactiveTime") LocalDateTime inactiveTime);

    /**
     * 查询所有用户排名数据（分页）
     */
    @Select("SELECT " +
            "uo.user_id as userId, " +
            "u.name, " +
            "IFNULL(uo.total_ac_num, 0) as totalAc, " +
            "IFNULL(uo.total_commit_num, 0) as totalSubmit, " +
            "uo.cache_time as lastUpdateTime " +
            "FROM ita_home.user_oj uo " +
            "INNER JOIN ita_home.user u ON uo.user_id = u.id " +
            "ORDER BY uo.total_ac_num DESC, uo.total_commit_num ASC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<UserRankingDto> findAllUserRankings(@Param("offset") int offset,
                                             @Param("limit") int limit);

    /**
     * 查询活跃用户排名数据（分页）
     */
    @Select("SELECT " +
            "uo.user_id as userId, " +
            "u.name as name, " +
            "uo.total_ac_num as totalAc, " +
            "uo.total_commit_num as totalSubmit, " +
            "uo.cache_time as lastUpdateTime " +
            "FROM ita_home.user_oj uo " +
            "INNER JOIN ita_home.user u ON uo.user_id = u.id " +
            "WHERE uo.total_ac_num > 0 " +
            "ORDER BY uo.total_ac_num DESC, uo.total_commit_num ASC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<UserRankingDto> findActiveUserRankings(@Param("offset") int offset,
                                                @Param("limit") int limit);

    /**
     * 统计所有用户数量
     */
    @Select("SELECT COUNT(*) FROM ita_home.user_oj uo INNER JOIN ita_home.user u ON uo.user_id = u.id")
    Long countAllUsers();

    /**
     * 统计活跃用户数量
     */
    @Select("SELECT COUNT(*) FROM ita_home.user_oj uo " +
            "INNER JOIN ita_home.user u ON uo.user_id = u.id " +
            "WHERE uo.total_ac_num > 0")
    Long countActiveUsers();

    /**
     * 查询指定用户数据
     */
    @Select("SELECT " +
            "uo.user_id as userId, " +
            "u.name as name, " +
            "IFNULL(uo.total_ac_num, 0) as totalAc, " +
            "IFNULL(uo.total_commit_num, 0) as totalSubmit, " +
            "uo.cache_time as lastUpdateTime " +
            "FROM ita_home.user_oj uo " +
            "INNER JOIN ita_home.user u ON uo.user_id = u.id " +
            "WHERE uo.user_id = #{userId}")
    UserRankingDto findUserDataById(@Param("userId") Long userId);

    /**
     * 计算比指定用户成绩更好的用户数量
     */
    @Select("SELECT COUNT(*) FROM ita_home.user_oj " +
            "WHERE (total_ac_num > #{totalAc} " +
            "OR (total_ac_num = #{totalAc} AND total_commit_num < #{totalSubmit})) " +
            "AND total_ac_num > 0")
    Integer countBetterUsers(@Param("totalAc") Integer totalAc,
                             @Param("totalSubmit") Integer totalSubmit);
}