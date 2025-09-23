package com.ita.home.queue;

import com.ita.home.model.event.EmailEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/23 17:19
 * 邮箱事件的阻塞队列(JUC自带)
 */
@Component
public class EmailQueue {
    private static final int QUEUE_SIZE = 1000;

    /**
     * JUC的阻塞队列，线程安全，支持阻塞取/放
     */
    private final BlockingQueue<EmailEvent> queue = new LinkedBlockingQueue<>(QUEUE_SIZE);

    /**
     * 生产者入队（阻塞直到有空间）
     */
    public void put(EmailEvent event) throws InterruptedException {
        queue.put(event);
    }

    /**
     * 消费者出队（阻塞直到有任务）
     */
    public EmailEvent take() throws InterruptedException {
        return queue.take();
    }

    /**
     * 获取队列当前大小（用于监控）
     */
    public int size() {
        return queue.size();
    }
}
