package com.ita.home.model.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/23 17:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户的邮箱事件")
public class EmailEvent {

    @Schema(description = "目的地邮箱")
    private String toEmail;

    @Schema(description = "邮件主题")
    private String subject;

    @Schema(description = "邮件内容")
    private String content;

    @Schema(description = "记录重试次数")
    private int retryCount = 0;
}
