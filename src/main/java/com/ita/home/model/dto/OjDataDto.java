package com.ita.home.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/26 08:47
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "每个oj的返回数据")
public class OjDataDto {
    @Schema(description = "该oj的名字")
    private String name;
    @Schema(description = "该oj的通过数")
    private Integer solved;
    @Schema(description = "该oj的提交次数")
    private Integer submitted;
}
