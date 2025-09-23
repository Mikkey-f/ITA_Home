package com.ita.home.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 注册请求实体
 * 用户注册时传递的参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户注册请求")
public class RegisterRequest {
    
    /** 用户名 - 必填 */
    @NonNull
    @Schema(description = "用户名", example = "newuser", required = true, minLength = 3, maxLength = 20)
    private String name;
    
    /** 密码 - 必填 */
    @NonNull
    @Schema(description = "密码", example = "123456", required = true, minLength = 6, maxLength = 20)
    private String password;

    /** 邮箱 - 必填 */
    @NonNull
    @Schema(description = "邮箱", example = "1234@qq.com", required = true)
    private String email;

    /** 验证码 - 必填 */
    @NonNull
    @Schema(description = "验证码", example = "1234", required = true)
    private String code;


    /**
     * 重写toString方法 - 不显示密码信息，保证安全
     */
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "name='" + name + '\'' +
                ", password='***'" + // 不打印真实密码
                ", email='" + email + '\'' +
                '}';
    }
}