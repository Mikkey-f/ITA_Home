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
    private Long userId;
    private String name;
    private Integer totalAc;
    private Integer totalSubmit;
    private LocalDateTime lastUpdateTime;
}
