package com.ita.home.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/23 15:10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户密码修改请求")
public class UpdatePasswordRequest {
    /** 旧密码 - 必填 */
    @NonNull
    @Schema(description = "原密码", example = "12345", required = true, minLength = 3, maxLength = 20)
    private String oldPassword;

    /** 新密码 - 必填 */
    @NonNull
    @Schema(description = "新密码", example = "123456", required = true, minLength = 3, maxLength = 20)
    private String newPassword;

    /** 新密码 - 必填 */
    @NonNull
    @Schema(description = "确认新密码", example = "123456", required = true, minLength = 3, maxLength = 20)
    private String confirmPassword;


    /**
     * 重写toString方法 - 不显示密码信息，保证安全
     */
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "oldPassword='***''"  + '\'' +
                ", newPassword='***'" + // 不打印真实密码
                ", confirmPassword='****'"  + '\'' +
                '}';
    }
}
