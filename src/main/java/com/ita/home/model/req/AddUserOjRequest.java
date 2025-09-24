package com.ita.home.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加用户OJ账号请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "添加用户OJ账号请求")
public class AddUserOjRequest {

    /** OJ平台类型 - 1:leetcode_cn, 2:luogu, 3:codeforces, 4:nowcoder */
    @Schema(description = "OJ平台类型", example = "1", required = true)
    private Integer platformType;

    /** OJ平台用户名 */
    @Schema(description = "单个OJ平台类型对应的值", example = "user123")
    private String value;
}