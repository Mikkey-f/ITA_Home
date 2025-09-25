package com.ita.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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


}