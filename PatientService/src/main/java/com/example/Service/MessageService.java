package com.example.Service;

import com.example.Mapper.MessageMapper;
import com.example.pojo.entity.MessageRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    /**
     * 发送候补转正通知
     */
    @Transactional
    public void sendWaitingPromotedMessage(String patientId, String scheduleId, String doctorName, String timePeriod) {
        MessageRecord message = new MessageRecord();
        message.setTitle("候补转正通知");
        message.setContent(String.format("您在 %s 医生 %s 的候补已成功转正，请尽快完成支付。", 
                                        doctorName, timePeriod));
        message.setSenderType("system");
        message.setReceiverType("specific_patient");
        message.setReceiverId(patientId);
        message.setStatus("unsent");
        message.setReadStatus("unconfirmed");
        message.setOverTime(LocalDateTime.now().plusDays(7)); // 7天后过期
        
        int inserted = messageMapper.insertMessage(message);
        if (inserted > 0) {
            log.info("Waiting promoted message created for patient: {}, schedule: {}", patientId, scheduleId);
        }
    }

    /**
     * 发送候补过期通知
     */
    @Transactional
    public void sendWaitingExpiredMessage(String patientId, String scheduleId, String doctorName, String scheduleDate) {
        MessageRecord message = new MessageRecord();
        message.setTitle("候补已过期");
        message.setContent(String.format("您在 %s 医生 %s 的候补已过期，该排班已结束。", 
                                        doctorName, scheduleDate));
        message.setSenderType("system");
        message.setReceiverType("specific_patient");
        message.setReceiverId(patientId);
        message.setStatus("unsent");
        message.setReadStatus("unconfirmed");
        message.setOverTime(LocalDateTime.now().plusDays(3)); // 3天后过期
        
        int inserted = messageMapper.insertMessage(message);
        if (inserted > 0) {
            log.info("Waiting expired message created for patient: {}, schedule: {}", patientId, scheduleId);
        }
    }

    /**
     * 发送候补截止通知
     */
    @Transactional
    public void sendWaitingClosedMessage(String patientId, String scheduleId, String doctorName, String scheduleTime) {
        MessageRecord message = new MessageRecord();
        message.setTitle("候补已截止");
        message.setContent(String.format("您在 %s 医生 %s 的候补已截止（就诊前3小时停止候补），未能成功转正。", 
                                        doctorName, scheduleTime));
        message.setSenderType("system");
        message.setReceiverType("specific_patient");
        message.setReceiverId(patientId);
        message.setStatus("unsent");
        message.setReadStatus("unconfirmed");
        message.setOverTime(LocalDateTime.now().plusDays(3));
        
        int inserted = messageMapper.insertMessage(message);
        if (inserted > 0) {
            log.info("Waiting closed message created for patient: {}, schedule: {}", patientId, scheduleId);
        }
    }

    /**
     * 发送支付成功通知
     */
    @Transactional
    public void sendPaymentSuccessMessage(String patientId, String doctorName, String scheduleTime, String amount) {
        MessageRecord message = new MessageRecord();
        message.setTitle("支付成功");
        message.setContent(String.format("您已成功支付 %s 医生 %s 的挂号费 %s 元，请按时就诊。", 
                                        doctorName, scheduleTime, amount));
        message.setSenderType("system");
        message.setReceiverType("specific_patient");
        message.setReceiverId(patientId);
        message.setStatus("unsent");
        message.setReadStatus("unconfirmed");
        message.setOverTime(LocalDateTime.now().plusDays(30));
        
        int inserted = messageMapper.insertMessage(message);
        if (inserted > 0) {
            log.info("Payment success message created for patient: {}", patientId);
        }
    }

    /**
     * 发送退款成功通知
     */
    @Transactional
    public void sendRefundSuccessMessage(String patientId, String doctorName, String scheduleTime, String refundAmount, String refundRate) {
        MessageRecord message = new MessageRecord();
        message.setTitle("退款成功");
        message.setContent(String.format("您已成功取消 %s 医生 %s 的挂号，退款 %s 元（退款比例 %s%%）已返还至医保账户。", 
                                        doctorName, scheduleTime, refundAmount, refundRate));
        message.setSenderType("system");
        message.setReceiverType("specific_patient");
        message.setReceiverId(patientId);
        message.setStatus("unsent");
        message.setReadStatus("unconfirmed");
        message.setOverTime(LocalDateTime.now().plusDays(30));
        
        int inserted = messageMapper.insertMessage(message);
        if (inserted > 0) {
            log.info("Refund success message created for patient: {}", patientId);
        }
    }

    /**
     * 发送挂号成功通知
     */
    @Transactional
    public void sendRegistrationSuccessMessage(String patientId, String doctorName, String scheduleTime, String amount) {
        MessageRecord message = new MessageRecord();
        message.setTitle("挂号成功");
        message.setContent(String.format("您已成功挂号 %s 医生 %s 的号源，应付金额 %s 元，请尽快完成支付。", 
                                        doctorName, scheduleTime, amount));
        message.setSenderType("system");
        message.setReceiverType("specific_patient");
        message.setReceiverId(patientId);
        message.setStatus("unsent");
        message.setReadStatus("unconfirmed");
        message.setOverTime(LocalDateTime.now().plusDays(7));
        
        int inserted = messageMapper.insertMessage(message);
        if (inserted > 0) {
            log.info("Registration success message created for patient: {}", patientId);
        }
    }

    /**
     * 发送候补队列位置更新通知
     */
    @Transactional
    public void sendWaitingPositionUpdateMessage(String patientId, String scheduleId, String doctorName, int newPosition) {
        MessageRecord message = new MessageRecord();
        message.setTitle("候补位置更新");
        message.setContent(String.format("您在 %s 医生的候补队列中的位置已更新为第 %d 位。", 
                                        doctorName, newPosition));
        message.setSenderType("system");
        message.setReceiverType("specific_patient");
        message.setReceiverId(patientId);
        message.setStatus("unsent");
        message.setReadStatus("unconfirmed");
        message.setOverTime(LocalDateTime.now().plusDays(3));
        
        int inserted = messageMapper.insertMessage(message);
        if (inserted > 0) {
            log.info("Waiting position update message created for patient: {}, new position: {}", patientId, newPosition);
        }
    }

    /**
     * 发送医保余额不足通知
     */
    @Transactional
    public void sendInsufficientBalanceMessage(String patientId, String currentBalance, String requiredAmount) {
        MessageRecord message = new MessageRecord();
        message.setTitle("医保余额不足");
        message.setContent(String.format("您的医保账户余额不足，当前余额 %s 元，需支付 %s 元，请充值后重试。", 
                                        currentBalance, requiredAmount));
        message.setSenderType("system");
        message.setReceiverType("specific_patient");
        message.setReceiverId(patientId);
        message.setStatus("unsent");
        message.setReadStatus("unconfirmed");
        message.setOverTime(LocalDateTime.now().plusDays(7));
        
        int inserted = messageMapper.insertMessage(message);
        if (inserted > 0) {
            log.info("Insufficient balance message created for patient: {}", patientId);
        }
    }

    /**
     * 发送加号成功通知（医生端）
     */
    @Transactional
    public void sendAddNumberSuccessMessage(String doctorId, String patientName, String scheduleTime) {
        MessageRecord message = new MessageRecord();
        message.setTitle("加号成功");
        message.setContent(String.format("您已成功为患者 %s 加号，就诊时间：%s。", 
                                        patientName, scheduleTime));
        message.setSenderType("system");
        message.setReceiverType("specific_doctor");
        message.setReceiverId(doctorId);
        message.setStatus("unsent");
        message.setReadStatus("unconfirmed");
        message.setOverTime(LocalDateTime.now().plusDays(7));
        
        int inserted = messageMapper.insertMessage(message);
        if (inserted > 0) {
            log.info("Add number success message created for doctor: {}", doctorId);
        }
    }

    /**
     * 发送待支付订单超时提醒
     */
    @Transactional
    public void sendPaymentTimeoutReminderMessage(String patientId, String doctorName, String scheduleTime, String timeLeft) {
        MessageRecord message = new MessageRecord();
        message.setTitle("订单即将超时");
        message.setContent(String.format("您在 %s 医生 %s 的挂号订单还有 %s 未支付，请尽快完成支付，否则订单将自动取消。",
                                        doctorName, scheduleTime, timeLeft));
        message.setSenderType("system");
        message.setReceiverType("specific_patient");
        message.setReceiverId(patientId);
        message.setStatus("unsent");
        message.setReadStatus("unconfirmed");
        message.setOverTime(LocalDateTime.now().plusHours(2));

        int inserted = messageMapper.insertMessage(message);
        if (inserted > 0) {
            log.info("Payment timeout reminder message created for patient: {}", patientId);
        }
    }

    /**
     * 发送支付超时取消通知
     */
    @Transactional
    public void sendPaymentTimeoutCancelMessage(String patientId) {
        MessageRecord message = new MessageRecord();
        message.setTitle("订单已超时取消");
        message.setContent("您的挂号订单因超过2分钟未支付已被系统自动取消。如需挂号请重新申请。");
        message.setSenderType("system");
        message.setReceiverType("specific_patient");
        message.setReceiverId(patientId);
        message.setStatus("unsent");
        message.setReadStatus("unconfirmed");
        message.setOverTime(LocalDateTime.now().plusHours(24));

        int inserted = messageMapper.insertMessage(message);
        if (inserted > 0) {
            log.info("Payment timeout cancel message created for patient: {}", patientId);
        }
    }
}
