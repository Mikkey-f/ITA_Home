package com.ita.home.producer;

import com.ita.home.exception.BaseException;
import com.ita.home.model.event.EmailEvent;
import com.ita.home.queue.EmailQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/23 17:24
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EmailProducer {

    private final EmailQueue emailQueue;

    public boolean sendEmailEvent(EmailEvent emailEvent) throws InterruptedException {
        if (!checkEmailEvent(emailEvent)) {
            throw new RuntimeException();
        }
        emailQueue.put(emailEvent);
        return true;
    }


    private boolean checkEmailEvent(EmailEvent emailEvent) {
        return emailEvent != null && emailEvent.getToEmail() != null;
    }
}
