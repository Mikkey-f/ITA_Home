package com.ita.home.consumer;

import com.ita.home.constant.EmailConstant;
import com.ita.home.model.event.EmailEvent;
import com.ita.home.producer.EmailProducer;
import com.ita.home.queue.EmailQueue;
import com.ita.home.service.impl.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/23 18:44
 */
@Component
@Slf4j
public class EmailConsumer implements Runnable {

    private final EmailQueue emailQueue;
    private final EmailService emailService;

    @Autowired
    public EmailConsumer(EmailQueue emailQueue,
                         EmailService emailService) {
        this.emailQueue = emailQueue;
        this.emailService = emailService;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EmailEvent emailEvent = emailQueue.take();
                log.info("消费者取到邮箱事件：收件人={}，重试次数={}", emailEvent.getToEmail(), emailEvent.getRetryCount());

                boolean sendSuccess = emailService.sendHtmlMail(emailEvent.getToEmail(),
                        emailEvent.getSubject(),
                        emailEvent.getContent());

                // 发送失败：重试（不超过最大次数）
                if (!sendSuccess && emailEvent.getRetryCount() < EmailConstant.EMAIL_RETRY_MAX) {
                    emailEvent.setRetryCount(emailEvent.getRetryCount() + 1);
                    emailQueue.put(emailEvent); // 重新入队
                    log.warn("邮件发送失败，重试次数={}，重新入队：收件人={}", emailEvent.getRetryCount(), emailEvent.getToEmail());
                } else if (!sendSuccess) {
                    log.error("邮件发送失败，已达最大重试次数（{}次），放弃：收件人={}", EmailConstant.EMAIL_RETRY_MAX, emailEvent.getToEmail());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
