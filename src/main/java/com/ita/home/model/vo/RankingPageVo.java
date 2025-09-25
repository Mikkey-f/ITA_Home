package com.ita.home.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/25 19:05
 */
@Data
@Builder
@Schema(description = "排名分页结果")
public class RankingPageVo {
    @Schema(description = "排名列表")
    private List<UserRankingVo> rankings;

    @Schema(description = "总记录数", example = "1000")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "总页数", example = "50")
    private Integer totalPages;

    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;

    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrevious;
}
