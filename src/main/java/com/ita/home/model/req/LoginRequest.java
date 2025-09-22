package com.ita.home.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 登录请求实体
 * 用户登录时传递的参数
 */
@Data // 生成getter/setter/equals/hashCode
@NoArgsConstructor // 无参构造器
@AllArgsConstructor // 全参构造器
@Schema(description = "用户登录请求")
public class LoginRequest {
    
    /** 用户名 - 必填 */
    @NonNull
    @Schema(description = "用户名", example = "admin", required = true, minLength = 3, maxLength = 20)
    private String name;
    
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
                "name='" + name + '\'' +
                ", password='***'" + // 不打印真实密码
                '}';
    }
}