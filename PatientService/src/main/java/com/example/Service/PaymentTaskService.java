package com.example.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 支付订单定时任务服务
 * 负责处理支付超时相关的定时任务
 */
@Service
@Slf4j
public class PaymentTaskService {

    @Autowired
    private PaymentService paymentService;

    /**
     * 定时处理超时未支付的订单
     * 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void processTimeoutPayments() {
        log.debug("Starting timeout payments processing task...");
        try {
            paymentService.processTimeoutPayments();
        } catch (Exception e) {
            log.error("Error processing timeout payments: {}", e.getMessage(), e);
        }
    }

    /**
     * 定时发送支付超时提醒
     * 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void sendPaymentTimeoutReminders() {
        log.debug("Starting payment timeout reminders task...");
        try {
            paymentService.sendPaymentTimeoutReminders();
        } catch (Exception e) {
            log.error("Error sending payment timeout reminders: {}", e.getMessage(), e);
        }
    }
}
