package com.ita.home.controller;

import com.ita.home.annotation.RequireAuth;
import com.ita.home.enums.OjPlatformEnum;
import com.ita.home.model.entity.UserOj;
import com.ita.home.model.req.RankingRequest;
import com.ita.home.model.req.UpdateUserOjRequest;
import com.ita.home.model.vo.OjUserDataVo;
import com.ita.home.model.vo.RankingPageVo;
import com.ita.home.model.vo.UserPlatformRankingVo;
import com.ita.home.model.vo.UserRankingVo;
import com.ita.home.result.Result;
import com.ita.home.service.UserOjService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

    /**
     * 获取用户刷题排名
     */
    @PostMapping("/ranking")
    @Operation(summary = "获取用户刷题排名", description = "根据AC数和提交数获取用户排名，支持分页")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "OJ账号不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @RequireAuth
    public Result<RankingPageVo> getUserRanking(@Valid @RequestBody RankingRequest request) {
        try {
            RankingPageVo rankingPage = userOjService.getUserRanking(request);
            return Result.success(rankingPage);
        } catch (Exception e) {
            log.error("获取用户排名失败", e);
            return Result.error("获取用户排名失败");
        }
    }

    /**
     * 获取用户刷题排名
     */
    @PostMapping("/refresh")
    @Operation(summary = "直接刷新内存中的oj信息", description = "刷新oj信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "OJ账号不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @RequireAuth
    public Result<OjUserDataVo> refreshUserOj(HttpServletRequest httpRequest) {
        try {
            Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
            OjUserDataVo realTimeOjUserDataVo = userOjService.getRealTimeOjUserDataVo(currentUserId);
            return Result.success(realTimeOjUserDataVo);
        } catch (Exception e) {
            log.error("获取用户排名失败", e);
            return Result.error("获取用户排名失败");
        }
    }

    /**
     * 获取指定用户的排名信息
     */
    @GetMapping("/ranking/{userId}")
    @Operation(summary = "获取指定用户排名", description = "获取指定用户在排行榜中的位置和信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "OJ账号不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @RequireAuth
    public Result<UserRankingVo> getUserRankById(@PathVariable Long userId) {
        try {
            UserRankingVo userRanking = userOjService.getUserRankById(userId);
            if (userRanking == null) {
                return Result.error("用户排名信息不存在");
            }
            return Result.success(userRanking);
        } catch (Exception e) {
            log.error("获取用户{}排名失败", userId, e);
            return Result.error("获取用户排名失败");
        }
    }

    /**
     * 获取用户在指定OJ平台的排名信息
     */
    @GetMapping("/platform-ranking/{platformId}")
    @Operation(summary = "获取用户在指定平台的排名信息", description = "查询用户在指定OJ平台的排名、AC数、提交数等信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "OJ账号不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @RequireAuth
    public Result<UserPlatformRankingVo> getUserPlatformRanking(
            @PathVariable @Schema(description = "平台ID", example = "luogu", allowableValues = {"leetcode", "luogu", "codeforces", "nowcoder"})
            String platformId,
            HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        UserPlatformRankingVo ranking = userOjService.getUserPlatformRanking(platformId, currentUserId);
        return Result.success(ranking);
    }
}