package com.ita.home.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户平台排名缓存实体类
 * 对应数据库中的user_platform_ranking表
 *
 * @Author: Mikkeyf
 * @CreateTime: 2025-09-26
 */
@TableName("user_platform_ranking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户平台排名缓存信息")
public class UserPlatformRanking {

    /** 主键ID - 自动递增 */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1")
    private Long id;

    /** 用户ID */
    @NonNull
    @TableField("user_id")
    @Schema(description = "用户ID", example = "123")
    private Long userId;

    /** 平台ID */
    @NonNull
    @TableField("platform_id")
    @Schema(description = "平台ID", example = "luogu", allowableValues = {"leetcode", "luogu", "codeforces", "nowcoder"})
    private String platformId;

    /** 平台名称 */
    @NonNull
    @TableField("platform_name")
    @Schema(description = "平台名称", example = "洛谷")
    private String platformName;

    /** 平台用户名 */
    @NonNull
    @TableField("username")
    @Schema(description = "平台用户名", example = "user123")
    private String username;

    /** 排名 */
    @NonNull
    @TableField("ranking")
    @Schema(description = "用户在该平台的排名", example = "25")
    private Integer ranking;

    /** AC数量 */
    @NonNull
    @TableField("ac_count")
    @Schema(description = "该平台AC数量", example = "120")
    private Integer acCount;

    /** 提交数量 */
    @NonNull
    @TableField("submit_count")
    @Schema(description = "该平台提交数量", example = "200")
    private Integer submitCount;

    /** 该平台总用户数 */
    @NonNull
    @TableField("total_users")
    @Schema(description = "该平台总用户数", example = "1500")
    private Integer totalUsers;

    /** 排名百分比 */
    @NonNull
    @TableField("ranking_percentage")
    @Schema(description = "排名百分比(保留2位小数)", example = "1.67")
    private BigDecimal rankingPercentage;

    /** 上次计算时间 */
    @NonNull
    @TableField("last_calc_time")
    @Schema(description = "上次排名计算时间")
    private LocalDateTime lastCalcTime;

    /** 创建时间 */
    @TableField("create_time")
    @Schema(description = "创建时间", example = "2025-09-26T10:30:00")
    private LocalDateTime createTime;

    /** 修改时间 */
    @TableField("update_time")
    @Schema(description = "修改时间", example = "2025-09-26T10:30:00")
    private LocalDateTime updateTime;

    /**
     * 便捷构造方法 - 创建排名记录
     */
    public UserPlatformRanking(@NonNull Long userId, @NonNull String platformId,
                               @NonNull String platformName, @NonNull String username) {
        this.userId = userId;
        this.platformId = platformId;
        this.platformName = platformName;
        this.username = username;
    }

    /**
     * 判断排名缓存是否有效（基于计算时间）
     */
    public boolean isRankingValid(int validMinutes) {
        return this.lastCalcTime.isAfter(LocalDateTime.now().minusMinutes(validMinutes));
    }

    /**
     * 获取排名百分比的double值
     */
    public Double getRankingPercentageAsDouble() {
        return this.rankingPercentage.doubleValue();
    }

    /**
     * 自定义toString方法
     */
    @Override
    public String toString() {
        return "UserPlatformRanking{" +
                "id=" + id +
                ", userId=" + userId +
                ", platformId='" + platformId + '\'' +
                ", platformName='" + platformName + '\'' +
                ", username='" + username + '\'' +
                ", ranking=" + ranking +
                ", acCount=" + acCount +
                ", submitCount=" + submitCount +
                ", totalUsers=" + totalUsers +
                ", rankingPercentage=" + rankingPercentage +
                ", lastCalcTime=" + lastCalcTime +
                '}';
    }
}