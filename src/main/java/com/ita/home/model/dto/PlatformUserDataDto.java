package com.ita.home.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 平台用户数据DTO
 * 用于排名计算时的数据传输
 *
 * @Author: Mikkeyf
 * @CreateTime: 2025-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "平台用户数据")
public class PlatformUserDataDto {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String realUsername;

    @Schema(description = "平台用户名")
    private String username;

    @Schema(description = "AC数量")
    private Integer acCount;

    @Schema(description = "提交数量")
    private Integer submitCount;

    @Schema(description = "最后更新时间")
    private java.time.LocalDateTime updateTime;
}
