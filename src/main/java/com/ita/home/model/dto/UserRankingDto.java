package com.ita.home.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/25 19:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户OJ排名")
public class UserRankingDto {
    @Schema(description = "用户排名")
    private Integer rank;
    @Schema(description = "用户id")
    private Long userId;
    @Schema(description = "用户名")
    private String name;
    @Schema(description = "oj平台总过的数")
    private Integer totalAc;
    @Schema(description = "oj平台总提交数")
    private Integer totalSubmit;
    @Schema(description = "上一次更新时间")
    private LocalDateTime lastUpdateTime;
}
