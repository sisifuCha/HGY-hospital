package com.example.Service;

import com.example.Mapper.PaymentMapper;
import com.example.Mapper.RegistrationMapper;
import com.example.pojo.dto.WaitingDto;
import com.example.pojo.entity.PayRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 实现：支付超时 15 分钟自动取消 + 候补自动转正并生成订单
 */
@Service
@Slf4j
public class PaymentTimeoutServiceImpl implements PaymentTimeoutService {

    private static final String WAITING_QUEUE_PREFIX = "waiting:queue:";
    private static final String WAITING_SET_PREFIX = "waiting:set:";
    private static final String WAITING_PATIENT_SCHEDULES_PREFIX = "waiting:patient_schedules:";

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** 超时分钟数：默认 15 */
    @Value("${patient.payment.timeoutMinutes:15}")
    private int timeoutMinutes;

    /** 扫描频率：默认每 60 秒 */
    @Scheduled(fixedDelayString = "${patient.payment.timeoutScanDelayMs:60000}")
    public void scheduledScan() {
        try {
            int n = processExpiredUnpaidPayments();
            if (n > 0) {
                log.info("[payment-timeout] processed {} expired unpaid payments", n);
            }
        } catch (Exception e) {
            // 定时任务不可崩溃主线程
            log.error("[payment-timeout] scheduledScan failed: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public int processExpiredUnpaidPayments() {
        var expired = paymentMapper.findExpiredUnpaidPayments(timeoutMinutes);
        if (expired == null || expired.isEmpty()) {
            return 0;
        }

        int processed = 0;
        for (PayRecord pr : expired) {
            if (pr == null || pr.getId() == null) {
                continue;
            }
            try {
                boolean done = processSingleExpiredPayment(pr);
                if (done) {
                    processed++;
                }
            } catch (Exception e) {
                log.error("[payment-timeout] process payment {} failed: {}", pr.getId(), e.getMessage(), e);
            }
        }
        return processed;
    }

    /**
     * 处理单个超时订单：
     * 1) 幂等取消 pay_record
     * 2) 将原挂号记录置为已取消（若存在）
     * 3) 若有候补队列：pop 第一位 -> 插入挂号(待支付) -> 创建支付订单；否则回补号源
     */
    private boolean processSingleExpiredPayment(PayRecord pr) {
        int canceled = paymentMapper.cancelPaymentIfUnpaid(pr.getId());
        if (canceled == 0) {
            // 已被支付/取消，跳过
            return false;
        }

        String patientId = pr.getPatientId();
        String scheduleRecordId = pr.getSchId();

        // 取消原挂号
        if (patientId != null && scheduleRecordId != null) {
            registrationMapper.updateRegistrationStatusToCanceled(patientId, scheduleRecordId);
        }

        // 候补转正：复用现有 cancelRegistration 内的“从 waiting:queue pop 并生成订单”的逻辑
        String queueKey = WAITING_QUEUE_PREFIX + scheduleRecordId;
        Object nextPatientObj = redisTemplate.opsForList().leftPop(queueKey);

        if (nextPatientObj != null) {
            WaitingDto waitingDto = (WaitingDto) nextPatientObj;
            String nextPatientId = waitingDto.getPatientId();
            log.info("[payment-timeout] promote waiting patient {} for schedule {}", nextPatientId, scheduleRecordId);

            int inserted = registrationMapper.insertRegistration(nextPatientId, scheduleRecordId, "待支付");
            if (inserted > 0) {
                try {
                    paymentService.createPayment(nextPatientId, scheduleRecordId);
                } catch (Exception e) {
                    // 生成订单失败：回补号源 + 回滚挂号
                    log.error("[payment-timeout] createPayment for promoted patient {} failed: {}", nextPatientId, e.getMessage(), e);
                    registrationMapper.updateRegistrationStatusToCanceled(nextPatientId, scheduleRecordId);
                    registrationMapper.incrementScheduleLeftSource(scheduleRecordId);
                    return true;
                }

                // 清理 Redis set
                String setKey = WAITING_SET_PREFIX + scheduleRecordId;
                String patientSchedulesKey = WAITING_PATIENT_SCHEDULES_PREFIX + nextPatientId;
                redisTemplate.opsForSet().remove(setKey, nextPatientId);
                redisTemplate.opsForSet().remove(patientSchedulesKey, scheduleRecordId);
            } else {
                // 插入挂号失败：回补号源
                registrationMapper.incrementScheduleLeftSource(scheduleRecordId);
            }
        } else {
            // 无候补：回补号源
            if (scheduleRecordId != null) {
                registrationMapper.incrementScheduleLeftSource(scheduleRecordId);
            }
        }

        return true;
    }
}

