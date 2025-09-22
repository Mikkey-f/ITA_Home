package com.ita.home.annotation;

import java.lang.annotation.*;

/**
 * 需要身份验证的注解
 * 
 * 标记在Controller方法上，表示该接口需要JWT身份验证
 * 拦截器会检查请求头中的JWT令牌，验证用户身份
 * 
 * 使用示例：
 * <pre>
 * {@code
 * @RequireAuth
 * @GetMapping("/profile")
 * public Result<User> getUserProfile() {
 *     // 需要登录才能访问的接口
 * }
 * 
 * @RequireAuth(required = false)
 * @GetMapping("/public")
 * public Result<String> getPublicInfo() {
 *     // 可选登录的接口，登录后可获得更多信息
 * }
 * }
 * </pre>
 * 
 * @author ITA Team
 * @since 2025-09-22
 */
@Target({ElementType.METHOD, ElementType.TYPE})  // 可以用在方法和类上
@Retention(RetentionPolicy.RUNTIME)             // 运行时保留注解信息
@Documented                                     // 生成JavaDoc时包含此注解
public @interface RequireAuth {
    
    /**
     * 是否必须登录
     * 
     * @return true=必须登录（默认），false=可选登录
     */
    boolean required() default true;
    
    /**
     * 角色要求（暂时预留，后续可扩展角色权限）
     * 
     * @return 所需角色数组，空数组表示不检查角色
     */
    String[] roles() default {};
    
    /**
     * 权限要求（暂时预留，后续可扩展权限系统）
     * 
     * @return 所需权限数组，空数组表示不检查权限
     */
    String[] permissions() default {};
}