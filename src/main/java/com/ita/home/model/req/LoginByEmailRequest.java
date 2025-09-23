package com.ita.home.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/23 12:31
 */
@Data // 生成getter/setter/equals/hashCode
@NoArgsConstructor // 无参构造器
@AllArgsConstructor // 全参构造器
@Schema(description = "用户使用邮箱的登录请求")
public class LoginByEmailRequest {

    /** 用户名 - 必填 */
    @NonNull
    @Schema(description = "邮箱", example = "123456@qq.com", required = true)
    private String email;

    /** 密码 - 必填 */
    @NonNull
    @Schema(description = "密码", example = "123456", required = true, minLength = 6, maxLength = 20)
    private String password;

    /**
     * 重写toString方法 - 不显示密码信息，保证安全
     */
    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", password='***'" + // 不打印真实密码
                '}';
    }
}
