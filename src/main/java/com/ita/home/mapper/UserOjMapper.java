package com.ita.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ita.home.model.dto.PlatformUserDataDto;
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
            "luogu_ac_num = #{luoguAcNum}, " +
            "luogu_submit_num = #{luoguSubmitNum}, " +
            "leetcode_ac_num = #{leetcodeAcNum}, " +
            "leetcode_submit_num = #{leetcodeSubmitNum}, " +
            "nowcoder_ac_num = #{nowcoderAcNum}, " +
            "nowcoder_submit_num = #{nowcoderSubmitNum}, " +
            "codeforces_ac_num = #{codeforceAcNum}, " +
            "codeforces_submit_num = #{codeforceSubmitNum}, " +
            "cache_time = #{cacheTime}, " +
            "last_access_time = #{lastAccessTime}, " +
            "update_time = #{updateTime} " +
            "WHERE user_id = #{userId}")
    int updateCacheData(@Param("userId") Long userId,
                        @Param("totalAcNum") Integer totalAcNum,
                        @Param("totalCommitNum") Integer totalCommitNum,
                        @Param("luoguAcNum") Integer luoguAcNum,
                        @Param("luoguSubmitNum") Integer luoguSubmitNum,
                        @Param("leetcodeAcNum") Integer leetcodeAcNum,
                        @Param("leetcodeSubmitNum") Integer leetcodeSubmitNum,
                        @Param("nowcoderAcNum") Integer nowcoderAcNum,
                        @Param("nowcoderSubmitNum") Integer nowcoderSubmitNum,
                        @Param("codeforceAcNum") Integer codeforceAcNum,
                        @Param("codeforceSubmitNum") Integer codeforceSubmitNum,
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


    /**
     * 计算用户在指定平台的排名
     * 排名规则：按AC数降序，AC数相同时按提交数升序
     */
    @Select("SELECT COUNT(*) + 1 FROM ita_home.user_oj " +
            "WHERE CASE #{platformId} " +
            "    WHEN 'luogu' THEN luogu_username IS NOT NULL AND luogu_username != '' " +
            "        AND (luogu_ac_num > #{acCount} OR (luogu_ac_num = #{acCount} AND luogu_submit_num < #{submitCount})) " +
            "    WHEN 'leetcode' THEN leetcode_cn_username IS NOT NULL AND leetcode_cn_username != '' " +
            "        AND (leetcode_ac_num > #{acCount} OR (leetcode_ac_num = #{acCount} AND leetcode_submit_num < #{submitCount})) " +
            "    WHEN 'nowcoder' THEN nowcoder_user_id IS NOT NULL AND nowcoder_user_id != '' " +
            "        AND (nowcoder_ac_num > #{acCount} OR (nowcoder_ac_num = #{acCount} AND nowcoder_submit_num < #{submitCount})) " +
            "    WHEN 'codeforces' THEN codeforce_username IS NOT NULL AND codeforce_username != '' " +
            "        AND (codeforces_ac_num > #{acCount} OR (codeforces_ac_num = #{acCount} AND codeforces_submit_num < #{submitCount})) " +
            "    ELSE FALSE END")
    Integer calculateUserRanking(@Param("platformId") String platformId,
                                 @Param("acCount") Integer acCount,
                                 @Param("submitCount") Integer submitCount);

    /**
     * 获取指定平台有数据的总用户数
     */
    @Select("SELECT COUNT(*) FROM ita_home.user_oj " +
            "WHERE CASE #{platformId} " +
            "    WHEN 'luogu' THEN luogu_username IS NOT NULL AND luogu_username != '' " +
            "    WHEN 'leetcode' THEN leetcode_cn_username IS NOT NULL AND leetcode_cn_username != '' " +
            "    WHEN 'nowcoder' THEN nowcoder_user_id IS NOT NULL AND nowcoder_user_id != '' " +
            "    WHEN 'codeforces' THEN codeforce_username IS NOT NULL AND codeforce_username != '' " +
            "    ELSE FALSE END")
    Integer getTotalUsersWithPlatformData(@Param("platformId") String platformId);

    /**
     * 获取洛谷平台用户数据并按排名规则排序
     * 排名规则：AC数降序，AC数相同时提交数升序
     */
    @Select("SELECT " +
            "    uo.user_id AS userId, " +
            "    u.name AS realUsername, " +
            "    uo.luogu_username AS username, " +
            "    COALESCE(uo.luogu_ac_num, 0) AS acCount, " +
            "    COALESCE(uo.luogu_submit_num, 0) AS submitCount, " +
            "    uo.update_time AS updateTime " +
            "FROM ita_home.user_oj uo " +
            "INNER JOIN ita_home.user u ON uo.user_id = u.id " +
            "WHERE uo.luogu_username IS NOT NULL " +
            "  AND uo.luogu_username != '' " +
            "  AND uo.luogu_ac_num IS NOT NULL " +
            "ORDER BY uo.luogu_ac_num DESC, uo.luogu_submit_num ASC")
    List<PlatformUserDataDto> getLuoguUsersOrdered();

    /**
     * 获取LeetCode中国站用户数据并按排名规则排序
     * 排名规则：AC数降序，AC数相同时提交数升序
     */
    @Select("SELECT " +
            "    uo.user_id AS userId, " +
            "    u.name AS realUsername, " +
            "    uo.leetcode_cn_username AS username, " +
            "    COALESCE(uo.leetcode_ac_num, 0) AS acCount, " +
            "    COALESCE(uo.leetcode_submit_num, 0) AS submitCount, " +
            "    uo.update_time AS updateTime " +
            "FROM ita_home.user_oj uo " +
            "INNER JOIN ita_home.user u ON uo.user_id = u.id " +
            "WHERE uo.leetcode_cn_username IS NOT NULL " +
            "  AND uo.leetcode_cn_username != '' " +
            "  AND uo.leetcode_ac_num IS NOT NULL " +
            "ORDER BY uo.leetcode_ac_num DESC, uo.leetcode_submit_num ASC")
    List<PlatformUserDataDto> getLeetcodeUsersOrdered();

    /**
     * 获取牛客网用户数据并按排名规则排序
     * 排名规则：AC数降序，AC数相同时提交数升序
     */
    @Select("SELECT " +
            "    uo.user_id AS userId, " +
            "    u.name AS realUsername, " +
            "    uo.nowcoder_user_id AS username, " +
            "    COALESCE(uo.nowcoder_ac_num, 0) AS acCount, " +
            "    COALESCE(uo.nowcoder_submit_num, 0) AS submitCount, " +
            "    uo.update_time AS updateTime " +
            "FROM ita_home.user_oj uo " +
            "INNER JOIN ita_home.user u ON uo.user_id = u.id " +
            "WHERE uo.nowcoder_user_id IS NOT NULL " +
            "  AND uo.nowcoder_user_id != '' " +
            "  AND uo.nowcoder_ac_num IS NOT NULL " +
            "ORDER BY uo.nowcoder_ac_num DESC, uo.nowcoder_submit_num ")
    List<PlatformUserDataDto> getNowcoderUsersOrdered();

    /**
     * 获取Codeforces用户数据并按排名规则排序
     * 排名规则：AC数降序，AC数相同时提交数升序
     */
    @Select("SELECT " +
            "    uo.user_id AS userId, " +
            "    u.name AS realUsername, " +
            "    uo.codeforce_username AS username, " +
            "    COALESCE(uo.codeforces_ac_num, 0) AS acCount, " +
            "    COALESCE(uo.codeforces_submit_num, 0) AS submitCount, " +
            "    uo.update_time AS updateTime " +
            "FROM ita_home.user_oj uo " +
            "INNER JOIN ita_home.user u ON uo.user_id = u.id " +
            "WHERE uo.codeforce_username IS NOT NULL " +
            "  AND uo.codeforce_username != '' " +
            "  AND uo.codeforces_ac_num IS NOT NULL " +
            "ORDER BY uo.codeforces_ac_num DESC, uo.codeforces_submit_num ")
    List<PlatformUserDataDto> getCodeforcesUsersOrdered();
}