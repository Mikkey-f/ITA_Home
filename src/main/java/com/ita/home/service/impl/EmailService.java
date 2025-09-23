package com.ita.home.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/23 19:00
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${ita.mail.username}")
    private String fromEmail;

    /**
     * 发送html格式邮件
     * @param toEmail 收件人邮箱
     * @param subject 邮件主题
     * @param content HTML内容
     * @return 发送结果（true成功，false失败）
     */
    public boolean sendHtmlMail(String toEmail, String subject, String content) {
        try {
            // 构建MIME邮件（支持HTML）
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail); // 发件人
            helper.setTo(toEmail);     // 收件人
            helper.setSubject(subject); // 主题
            helper.setText(content, true); // 第二个参数true=HTML格式

            // 发送邮件
            javaMailSender.send(mimeMessage);
            log.info("HTML邮件发送成功：收件人={}", toEmail);
            return true;
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败：收件人={}", toEmail, e);
            return false;
        }
    }
}
