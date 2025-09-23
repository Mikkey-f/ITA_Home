package com.ita.home.controller;

import com.ita.home.annotation.RequireAuth;
import com.ita.home.model.entity.User;
import com.ita.home.model.req.LoginByNameRequest;
import com.ita.home.model.req.RegisterRequest;
import com.ita.home.result.Result;
import com.ita.home.service.UserService;
import com.ita.home.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    private JwtUtil jwtUtil;

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

            // 检查用户名是否已存在
            if (userService.isNameExist(registerRequest.getName())) {
                return Result.error("用户名已存在，请换一个试试");
            }

            // 检查邮箱是否已存在
            if (userService.isEmailExist(registerRequest.getEmail())) {
                return Result.error("邮箱已存在，请换一个试试");
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
    @Operation(summary = "用户使用用户名登录", description = "验证用户名和密码，返回JWT令牌和用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功，返回JWT令牌"),
            @ApiResponse(responseCode = "400", description = "用户名或密码错误")
    })
    @PostMapping("/login/name")
    public Result<Map<String, Object>> login(@RequestBody LoginByNameRequest loginRequest) {
        try {
            // 执行登录验证
            User user = userService.loginByName(loginRequest);
            if (user == null) {
                log.warn("登录失败，用户名或密码错误: {}", loginRequest.getName());
                return Result.error("用户名或密码错误");
            }

            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getId(), user.getName());

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);                    // JWT令牌
            data.put("tokenType", "Bearer");             // 令牌类型
            data.put("expiresIn", jwtUtil.getExpireHours() * 3600); // 过期时间（秒）
            
            // 用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("name", user.getName());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("createTime", user.getCreateTime());
            data.put("user", userInfo);

            log.info("用户登录成功: {} (ID: {})", user.getName(), user.getId());
            return Result.success(data);

        } catch (NullPointerException e) {
            log.error("登录请求参数为空: {}", e.getMessage());
            return Result.error("用户名和密码不能为空");
        } catch (Exception e) {
            log.error("登录过程中发生异常", e);
            return Result.error("系统异常，请联系管理员");
        }
    }

    /**
     * 根据用户ID查询用户信息（需要登录）
     * GET /api/user/{id}
     */
    @RequireAuth
    @Operation(summary = "查询用户信息", description = "根据用户ID获取用户详细信息（需要JWT认证）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "用户不存在"),
            @ApiResponse(responseCode = "401", description = "未授权，请先登录")
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
            user.setPassword("xxxx");
            return Result.success(user);

        } catch (Exception e) {
            log.error("查询用户信息失败", e);
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
            log.error("检查用户名是否存在失败", e);
            return Result.error("系统异常，请联系管理员");
        }
    }

    /**
     * 获取当前登录用户信息
     * GET /api/user/profile
     */
    @RequireAuth
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未授权，请先登录")
    })
    @GetMapping("/profile")
    public Result<Map<String, Object>> getCurrentUserProfile(HttpServletRequest request) {
        try {
            // 从请求属性中获取当前用户信息（由JWT过滤器设置）
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            String currentUsername = (String) request.getAttribute("currentUsername");
            
            if (currentUserId == null) {
                return Result.error("获取用户信息失败");
            }

            // 查询用户详细信息
            User user = userService.findById(currentUserId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 构建返回数据
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("name", user.getName());
            profile.put("avatar", user.getAvatar());
            profile.put("createTime", user.getCreateTime());
            profile.put("updateTime", user.getUpdateTime());

            return Result.success(profile);

        } catch (Exception e) {
            log.error("获取用户个人信息失败", e);
            return Result.error("系统异常，请联系管理员");
        }
    }

    /**
     * 修改用户头像
     * PUT /api/user/avatar
     */
    @RequireAuth
    @Operation(summary = "修改头像", description = "修改当前登录用户的头像")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "头像编号无效"),
            @ApiResponse(responseCode = "401", description = "未授权，请先登录")
    })
    @PutMapping("/avatar")
    public Result<String> updateAvatar(
            @Parameter(description = "头像编号（1-9）", required = true)
            @RequestParam Integer avatar,
            HttpServletRequest request) {
        try {
            // 参数验证
            if (avatar == null || avatar < 1 || avatar > 9) {
                return Result.error("头像编号必须在1-9之间");
            }

            // 获取当前用户ID
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            if (currentUserId == null) {
                return Result.error("获取用户信息失败");
            }

            // 更新头像
            boolean success = userService.updateUserAvatar(currentUserId, avatar);
            if (success) {
                log.info("用户 {} 修改头像成功，新头像: {}", currentUserId, avatar);
                return Result.success("头像修改成功");
            } else {
                return Result.error("头像修改失败");
            }

        } catch (Exception e) {
            log.error("修改用户头像失败", e);
            return Result.error("系统异常，请联系管理员");
        }
    }

    /**
     * 修改密码
     * PUT /api/user/password
     */
    @RequireAuth
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "原密码错误或新密码格式不正确"),
            @ApiResponse(responseCode = "401", description = "未授权，请先登录")
    })
    @PutMapping("/password")
    public Result<String> updatePassword(
            @RequestBody Map<String, String> passwordData,
            HttpServletRequest request) {
        try {
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            String confirmPassword = passwordData.get("confirmPassword");

            // 参数验证
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return Result.error("原密码不能为空");
            }
            if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
                return Result.error("新密码长度必须在6-20字符之间");
            }
            if (!newPassword.equals(confirmPassword)) {
                return Result.error("两次输入的新密码不一致");
            }

            // 获取当前用户ID
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            if (currentUserId == null) {
                return Result.error("获取用户信息失败");
            }

            // 更新密码
            boolean success = userService.updateUserPassword(currentUserId, oldPassword, newPassword);
            if (success) {
                log.info("用户 {} 修改密码成功", currentUserId);
                return Result.success("密码修改成功");
            } else {
                return Result.error("原密码错误");
            }

        } catch (Exception e) {
            log.error("修改用户密码失败", e);
            return Result.error("系统异常，请联系管理员");
        }
    }


}