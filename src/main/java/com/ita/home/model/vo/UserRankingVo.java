package com.ita.home.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/25 19:04
 */
@Data
@Builder
@Schema(description = "用户排名信息")
public class UserRankingVo {
    @Schema(description = "排名", example = "1")
    private Integer rank;

    @Schema(description = "用户ID", example = "123")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "总AC数", example = "150")
    private Integer totalAc;

    @Schema(description = "总提交数", example = "300")
    private Integer totalSubmit;

    @Schema(description = "AC率", example = "50.0")
    private Double acRate;

    @Schema(description = "最后更新时间", example = "2025-09-25T10:30:00")
    private LocalDateTime lastUpdateTime;
}
