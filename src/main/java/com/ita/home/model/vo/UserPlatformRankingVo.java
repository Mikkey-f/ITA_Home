package com.ita.home.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户平台排名信息")
public class UserPlatformRankingVo {

    @Schema(description = "平台ID")
    private String platformId;

    @Schema(description = "平台名称")
    private String platformName;

    @Schema(description = "用户在该平台的排名")
    private Integer ranking;

    @Schema(description = "该平台AC数")
    private Integer acCount;

    @Schema(description = "该平台提交数")
    private Integer submitCount;

    @Schema(description = "总用户数(该平台有数据的用户)")
    private Integer totalUsers;

    @Schema(description = "排名百分比(保留2位小数)")
    private Double rankingPercentage;

    @Schema(description = "用户名(该平台的用户名)")
    private String username;
}