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
//    public static final String EMAIL_CODE_TEMPLATE = "<h3>尊敬的用户：</h3>" +
//            "<p>您好！您正在进行ITAHome的身份验证操作，本次验证码为：</p>" +
//            // 验证码突出显示：红色+18号加粗字体，便于用户快速识别
//            "<p style='color:#E53935; font-size:18px; font-weight:bold; margin:15px 0;'>{0}</p>" +
//            // 扩展占位符1：验证码有效期（按需插入，无需时传空）
//            "<p>{1}</p>" +
//            // 扩展占位符2：额外安全提示（按需插入，无需时传空）
//            "<p>{2}</p>" +
//            "<p>若您未发起此操作，可能是他人误填了您的邮箱，请忽略此邮件，无需担心账号安全。</p>";
    public static final String EMAIL_CODE_TEMPLATE =
        "<div style='max-width: 600px; margin: 0 auto; background: white; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif;'>" +
            "<!-- 邮件头部 -->" +
            "<div style='background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding: 30px; text-align: center; color: white;'>" +
                "<h1 style='margin-bottom: 5px;'>ITAHome</h1>" +
            "</div>" +
            "<!-- 邮件内容 -->" +
            "<div style='padding: 40px 30px;'>" +
                "<div style='font-size: 20px; color: #1e293b; margin-bottom: 20px; font-weight: 600;'>尊敬的用户，您好！👋</div>" +
                "<div style='color: #475569; line-height: 1.6; margin-bottom: 30px; font-size: 16px;'>感谢您选择ITAHome！您正在进行身份验证操作，为了确保账户安全，请使用以下验证码完成验证。</div>" +
                "<!-- 验证码区域 -->" +
                "<div style='background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%); border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0; border-left: 4px solid #4f46e5;'>" +
                    "<div style='color: #64748b; font-size: 14px; margin-bottom: 10px; text-transform: uppercase; letter-spacing: 0.5px;'>您的验证码</div>" +
                    "<div style='font-size: 36px; font-weight: 800; color: #4f46e5; letter-spacing: 4px; margin: 15px 0; font-family: Courier New, monospace;'>{0}</div>" +
                    "<div style='background: #fef3c7; color: #92400e; padding: 12px 20px; border-radius: 8px; font-size: 14px; margin-top: 15px; display: inline-block; border-left: 3px solid #f59e0b;'>⏰ {1}</div>" +
                "</div>" +
                "<!-- 安全提示 -->" +
                "<div style='background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 20px; margin: 25px 0;'>" +
                    "<div style='color: #991b1b; font-size: 14px; line-height: 1.5;'>🔒 <strong>安全提醒：</strong>{2}</div>" +
                "</div>" +
                "<!-- 额外信息 -->" +
                "<div style='background: #f0f9ff; border-left: 3px solid #0ea5e9; padding: 20px; margin: 25px 0; border-radius: 0 8px 8px 0; color: #0c4a6e; font-size: 14px;'>" +
                    "<strong>💡 小贴士：</strong>若您未发起此操作，可能是他人误填了您的邮箱，请忽略此邮件，无需担心账号安全。" +
                "</div>" +
                "<div style='color: #475569; line-height: 1.6; margin-bottom: 30px; font-size: 16px;'>如果您在使用过程中遇到任何问题，欢迎随时联系我们的客服团队！</div>" +
            "</div>" +
            "<!-- 邮件底部 -->" +
            "<div style='background: #1e293b; color: #cbd5e1; padding: 30px; text-align: center; font-size: 12px;'>" +
                "© 2025 ITAHome. 保留所有权利。<br>此邮件由系统自动发送，请勿直接回复。" +
            "</div>" +
        "</div>";
}
