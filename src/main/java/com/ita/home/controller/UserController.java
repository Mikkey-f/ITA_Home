package com.ita.home.controller;

import com.ita.home.model.entity.User;
import com.ita.home.model.req.LoginRequest;
import com.ita.home.model.req.RegisterRequest;
import com.ita.home.result.Result;
import com.ita.home.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 用户控制器
 * 处理用户注册和登录相关的HTTP请求
 */
@RestController
@RequestMapping("/api/user")
@Slf4j
@Tag(name = "用户管理", description = "用户注册、登录、信息查询等接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     * POST /api/user/register
     */
    @Operation(summary = "用户注册", description = "创建新用户账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或用户名已存在")
    })
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // 业务验证
            if (registerRequest.getName().length() < 3 || registerRequest.getName().length() > 20) {
                return Result.error("用户名长度必须在3-20字符之间");
            }

            if (registerRequest.getPassword().length() < 6 || registerRequest.getPassword().length() > 20) {
                return Result.error("密码长度必须在6-20字符之间");
            }

            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return Result.error("两次输入的密码不一致");
            }

            // 检查用户名是否已存在
            if (userService.isNameExist(registerRequest.getName())) {
                return Result.error("用户名已存在，请换一个试试");
            }

            // 执行注册
            boolean success = userService.register(registerRequest);
            return success ? Result.success("注册成功！") : Result.error("注册失败，请稍后重试");

        } catch (NullPointerException e) {
            return Result.error("请求参数不能为空");
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error("系统异常，请联系管理员");
        }
    }

    /**
     * 用户登录接口
     * POST /api/user/login
     */
    @Operation(summary = "用户登录", description = "验证用户名和密码，返回用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "用户名或密码错误")
    })
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 执行登录
            User user = userService.login(loginRequest);
            if (user == null) {
                log.error("用户名或密码错误");
                return Result.error("用户名或密码错误");
            }

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("name", user.getName());
            data.put("avatar", user.getAvatar());
            data.put("createTime", user.getCreateTime());

            return Result.success(data);

        } catch (NullPointerException e) {
            log.error(e.getMessage());
            return Result.error("用户名和密码不能为空");
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error("系统异常，请联系管理员");
        }
    }

    /**
     * 根据用户ID查询用户信息
     * GET /api/user/{id}
     */
    @Operation(summary = "查询用户信息", description = "根据用户ID获取用户详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "用户不存在")
    })
    @GetMapping("/{id}")
    public Result<User> getUserById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.error("用户ID不能为空");
            }

            User user = userService.findById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 清除密码信息
            user.setPassword(null);
            return Result.success(user);

        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error("系统异常，请联系管理员");
        }
    }

    /**
     * 检查用户名是否存在
     * GET /api/user/check/{name}
     */
    @Operation(summary = "检查用户名", description = "检查指定用户名是否已被注册")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查完成，返回true表示已存在，false表示可用")
    })
    @GetMapping("/check/{name}")
    public Result<Boolean> checkNameExist(
            @Parameter(description = "要检查的用户名", required = true)
            @PathVariable String name) {
        try {
            // 路径参数简单验证
            if (name == null || name.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }

            boolean exist = userService.isNameExist(name);
            return Result.success(exist);

        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error("系统异常，请联系管理员");
        }
    }
}