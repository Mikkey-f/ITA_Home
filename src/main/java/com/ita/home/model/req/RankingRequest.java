package com.ita.home.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/25 19:04
 */
@Data
@Schema(description = "排名查询请求")
public class RankingRequest {
    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 20;

    @Schema(description = "是否只显示有数据的用户", example = "true")
    private Boolean onlyActiveUsers = true;
}
