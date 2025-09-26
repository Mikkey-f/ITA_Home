package com.ita.home.model.vo;

import com.ita.home.model.dto.OjDataDto;
import com.ita.home.model.dto.OjUserDataDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/25 00:32
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "OJ用户返回数据")
public class OjUserDataVo {

    @Schema(description = "各个平台返回的信息")
    private List<OjDataDto> ojDataDtoList;

    @Schema(description = "总ac数")
    private Integer totalAc;

    @Schema(description = "总submit数")
    private Integer totalSubmit;
}
