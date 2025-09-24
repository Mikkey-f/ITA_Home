package com.ita.home.controller;

import com.ita.home.annotation.RequireAuth;
import com.ita.home.enums.OjPlatformEnum;
import com.ita.home.model.entity.UserOj;
import com.ita.home.model.req.UpdateUserOjRequest;
import com.ita.home.result.Result;
import com.ita.home.service.UserOjService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户OJ平台账号控制器
 * 处理用户OJ账号管理相关的HTTP请求
 */
@RestController
@RequestMapping("/api/user-oj")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户OJ管理", description = "用户OJ平台账号绑定、数据同步等接口")
public class UserOjController {

    private final UserOjService userOjService;

    /**
     * 更新用户OJ账号
     */
    @PutMapping("/update")
    @RequireAuth
    @Operation(summary = "更新用户OJ账号", description = "更新当前用户的OJ平台账号信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "OJ账号不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> updateUserOjAccount(
            @RequestBody UpdateUserOjRequest request,
            HttpServletRequest httpRequest) {

        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");

        // 验证平台类型
        OjPlatformEnum platform = OjPlatformEnum.getByPlatformId(request.getPlatformType());
        if (platform == null) {
            return Result.error("无效的平台类型");
        }

        // 获取现有的OJ账号
        UserOj userOj = userOjService.getUserOjAccount(currentUserId);
        if (userOj == null) {
            return Result.error("Oj表没有成功创建用户表项");
        }

        // 更新字段
        switch (request.getPlatformType()) {
            // 力扣
            case "leetcode": userOj.setLeetcodeCnUsername(request.getValue()); break;
            // 洛谷
            case "luogu": userOj.setLuoguUsername(request.getValue()); break;
            // cf
            case "codeforces": userOj.setCodeforceUsername(request.getValue()); break;
            // nowcoder
            case "nowcoder": userOj.setNowcoderUserId(request.getValue()); break;
            default: return Result.error("没有该类型的oj平台");
        }

        boolean success = userOjService.updateUserOjAccount(userOj);

        if (success) {
            log.info("用户{}更新OJ账号成功: platform={}", currentUserId, platform.getPlatformName());
            return Result.success("更新OJ账号成功");
        } else {
            return Result.error("更新OJ账号失败");
        }
    }

}