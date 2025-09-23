package com.ita.home.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/23 18:53
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailConstant {
    // 激活码Redis前缀（key格式：ACTIVATION_CODE:用户ID）
    public static final String ACTIVATION_CODE_PREFIX = "ACTIVATION_CODE:";
    // 激活码有效期：30分钟（单位：秒）
    public static final long ACTIVATION_CODE_EXPIRE_SEC = 30 * 60;
    // 邮件发送重试次数（防止临时网络失败）
    public static final int EMAIL_RETRY_MAX = 3;
    // 邮件主题
    public static final String EMAIL_SUBJECT = "【ITAHome】账号验证码通知";

    // 扩展内容1
    public static final String validTimeTip = "验证码10分钟内有效，过期需重新获取";
    // 扩展内容2
    public static final String securityTip = "验证码仅用于身份验证，请勿向任何第三方泄露";
    /**
     * 验证码邮件内容模板（HTML格式）
     * 占位符说明：
     * {0} → 核心：验证码（如6位数字）
     * {1} → 扩展：验证码有效期（如“10分钟内有效”，可选，无需时传空字符串）
     * {2} → 扩展：额外安全提示（如“请勿向他人泄露验证码”，可选，无需时传空字符串）
     */
    public static final String EMAIL_CODE_TEMPLATE = "<h3>尊敬的用户：</h3>" +
            "<p>您好！您正在进行ITAHome的身份验证操作，本次验证码为：</p>" +
            // 验证码突出显示：红色+18号加粗字体，便于用户快速识别
            "<p style='color:#E53935; font-size:18px; font-weight:bold; margin:15px 0;'>{0}</p>" +
            // 扩展占位符1：验证码有效期（按需插入，无需时传空）
            "<p>{1}</p>" +
            // 扩展占位符2：额外安全提示（按需插入，无需时传空）
            "<p>{2}</p>" +
            "<p>若您未发起此操作，可能是他人误填了您的邮箱，请忽略此邮件，无需担心账号安全。</p>";
}
