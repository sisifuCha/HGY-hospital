package com.example.Service;

import com.example.Mapper.PaymentMapper;
import com.example.Mapper.RefundMapper;
import com.example.Mapper.RegistrationMapper;
import com.example.Mapper.WaitingMapper;
import com.example.conmon.exception.CreateFailedException;
import com.example.pojo.dto.PaymentDto;
import com.example.pojo.entity.MedicalInsurance;
import com.example.pojo.entity.PayRecord;
import com.example.pojo.entity.ReimburseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private RegistrationMapper registrationMapper;
    
    @Autowired
    private RefundMapper refundMapper;
    
    @Autowired
    private WaitingMapper waitingMapper;
    
    @Autowired
    private MessageService messageService;

    @Override
    @Transactional
    public PaymentDto createPayment(String patientId, String scheduleRecordId) {
        // 1. 检查是否已存在支付记录
        PayRecord existingPayment = paymentMapper.findPaymentByPatientAndSchedule(patientId, scheduleRecordId);
        if (existingPayment != null) {
            throw new IllegalArgumentException("该挂号已存在支付订单");
        }

        // 2. 检查挂号记录是否存在且状态为待支付
        String registrationStatus = registrationMapper.getRegistrationStatusByKey(patientId, scheduleRecordId);
        if (registrationStatus == null) {
            throw new IllegalArgumentException("挂号记录不存在");
        }
        if (!"待支付".equals(registrationStatus)) {
            throw new IllegalArgumentException("挂号状态不是待支付，无法创建订单");
        }

        // 3. 获取原始金额（挂号费）
        BigDecimal oriAmount = paymentMapper.getRegistrationFeeBySchedule(scheduleRecordId);
        if (oriAmount == null) {
            throw new IllegalArgumentException("无法获取挂号费用信息");
        }

        // 4. 获取报销信息并计算实际支付金额
        ReimburseType reimburseType = paymentMapper.getReimburseTypeByPatient(patientId);
        BigDecimal askPayAmount;
        if (reimburseType != null && reimburseType.getPercent() != null) {
            // 计算公式：实付金额 = 原价 * (1 - 报销比例/100)
            BigDecimal reimburseFactor = BigDecimal.ONE.subtract(
                reimburseType.getPercent().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            );
            askPayAmount = oriAmount.multiply(reimburseFactor).setScale(2, RoundingMode.HALF_UP);
        } else {
            // 无报销，全额支付
            askPayAmount = oriAmount;
        }

        // 5. 获取医生ID
        String doctorId = paymentMapper.getDoctorIdBySchedule(scheduleRecordId);

        // 6. 创建支付记录
        PayRecord payment = new PayRecord();
        payment.setId(UUID.randomUUID().toString());
        payment.setPayTime(ZonedDateTime.now());
        payment.setPayStatus("待支付");
        payment.setOriAmount(oriAmount);
        payment.setAskPayAmount(askPayAmount);
        payment.setPatientId(patientId);
        payment.setDocId(doctorId);
        payment.setSchId(scheduleRecordId);

        int inserted = paymentMapper.insertPayment(payment);
        if (inserted == 0) {
            throw new CreateFailedException();
        }

        log.info("Created payment order: {} for patient: {}, oriAmount: {}, askPayAmount: {}", 
                 payment.getId(), patientId, oriAmount, askPayAmount);

        // 7. 返回支付订单详情
        return paymentMapper.findPaymentById(payment.getId());
    }

    @Override
    public List<PaymentDto> getPaymentsByPatient(String patientId) {
        return paymentMapper.findPaymentsByPatient(patientId);
    }

    @Override
    public PaymentDto getPaymentById(String paymentId) {
        PaymentDto payment = paymentMapper.findPaymentById(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return payment;
    }

    @Override
    @Transactional
    public PaymentDto payOrder(String paymentId) {
        // 1. 查询订单信息
        PayRecord payment = paymentMapper.findPaymentInfoById(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        if (!"待支付".equals(payment.getPayStatus())) {
            throw new IllegalArgumentException("订单状态不是待支付，无法支付");
        }

        // 2. 查询完整订单信息以获取金额
        PaymentDto fullPayment = paymentMapper.findPaymentById(paymentId);
        BigDecimal askPayAmount = fullPayment.getAskPayAmount();

        // 3. 查询患者医保账户
        String medicalInsuranceId = paymentMapper.getPatientMedicalInsuranceId(payment.getPatientId());
        if (medicalInsuranceId == null) {
            throw new IllegalArgumentException("患者未绑定医保账户");
        }

        MedicalInsurance insurance = paymentMapper.getMedicalInsurance(medicalInsuranceId);
        if (insurance == null) {
            throw new IllegalArgumentException("医保账户不存在");
        }

        // 4. 检查医保余额
        if (insurance.getOverage().compareTo(askPayAmount) < 0) {
            throw new IllegalArgumentException("医保余额不足，当前余额：" + insurance.getOverage() + "，需支付：" + askPayAmount);
        }

        // 5. 扣减医保余额
        int deducted = paymentMapper.deductMedicalInsurance(medicalInsuranceId, askPayAmount);
        if (deducted == 0) {
            // 发送余额不足消息
            try {
                MedicalInsurance insuranceInfo = paymentMapper.getMedicalInsurance(medicalInsuranceId);
                String currentBalance = insuranceInfo != null && insuranceInfo.getOverage() != null 
                    ? insuranceInfo.getOverage().toString() : "0";
                messageService.sendInsufficientBalanceMessage(
                    payment.getPatientId(), 
                    currentBalance, 
                    askPayAmount.toString()
                );
            } catch (Exception e) {
                log.warn("Failed to send insufficient balance message: {}", e.getMessage());
            }
            throw new IllegalArgumentException("扣减医保余额失败，余额可能不足");
        }

        // 6. 更新支付状态
        int updated = paymentMapper.updatePaymentStatus(paymentId, "已支付");
        if (updated == 0) {
            throw new CreateFailedException();
        }

        // 7. 更新挂号记录状态为已挂号
        int regUpdated = registrationMapper.updateRegistrationStatusToConfirmed(payment.getPatientId(), payment.getSchId());
        if (regUpdated == 0) {
            log.warn("Failed to update registration status for payment: {}", paymentId);
        }

        log.info("Payment completed: {}, deducted {} from medical insurance: {}", 
                 paymentId, askPayAmount, medicalInsuranceId);

        // 8. 发送支付成功消息
        PaymentDto result = paymentMapper.findPaymentById(paymentId);
        try {
            String doctorName = result.getDoctorName() != null ? result.getDoctorName() : "医生";
            String scheduleTime = result.getPayTime() != null ? result.getPayTime().toString() : "就诊时段";
            messageService.sendPaymentSuccessMessage(
                payment.getPatientId(), 
                doctorName, 
                scheduleTime, 
                askPayAmount.toString()
            );
        } catch (Exception e) {
            log.warn("Failed to send payment success message: {}", e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional
    public PaymentDto cancelOrder(String paymentId) {
        // 1. 查询订单信息
        PayRecord payment = paymentMapper.findPaymentInfoById(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        if ("已取消".equals(payment.getPayStatus())) {
            throw new IllegalArgumentException("订单已取消，无法重复取消");
        }

        // 2. 检查订单是否过期（获取排班开始时间）
        LocalDateTime scheduleStartTime = waitingMapper.getScheduleStartTime(payment.getSchId());
        if (scheduleStartTime == null) {
            throw new IllegalArgumentException("无法获取排班信息");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(scheduleStartTime)) {
            throw new IllegalArgumentException("订单已过期，无法退款");
        }

        // 3. 计算退款比例
        BigDecimal refundRate = calculateRefundRate(now, scheduleStartTime);
        log.info("Calculated refund rate: {} for payment: {}", refundRate, paymentId);

        // 4. 如果已支付，退还医保余额（按比例）
        if ("已支付".equals(payment.getPayStatus())) {
            BigDecimal refundAmount = payment.getAskPayAmount()
                    .multiply(refundRate)
                    .setScale(2, RoundingMode.HALF_UP);
            
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                // 查询医保账户
                String medicalInsuranceId = paymentMapper.findMedicalInsuranceIdByPatient(payment.getPatientId());
                if (medicalInsuranceId != null) {
                    int refunded = paymentMapper.refundToMedicalInsurance(medicalInsuranceId, refundAmount);
                    if (refunded > 0) {
                        log.info("Refunded {} to medical insurance: {}", refundAmount, medicalInsuranceId);
                    }
                }
            }
            
            // 回补号源
            registrationMapper.incrementScheduleLeftSource(payment.getSchId());
            log.info("Source restored for cancelled paid order: {}", paymentId);
        }

        // 5. 更新支付状态为已取消
        int updated = paymentMapper.updatePaymentStatus(paymentId, "已取消");
        if (updated == 0) {
            throw new CreateFailedException();
        }

        // 6. 更新挂号记录状态为已取消
        int regUpdated = registrationMapper.updateRegistrationStatusToCanceled(payment.getPatientId(), payment.getSchId());
        if (regUpdated == 0) {
            log.warn("Failed to update registration status for cancelled payment: {}", paymentId);
        }

        log.info("Order cancelled: {}, refund rate: {}", paymentId, refundRate);

        // 7. 发送退款成功消息
        PaymentDto result = paymentMapper.findPaymentById(paymentId);
        if ("已支付".equals(payment.getPayStatus()) && refundRate.compareTo(BigDecimal.ZERO) > 0) {
            try {
                BigDecimal refundAmount = payment.getAskPayAmount()
                        .multiply(refundRate)
                        .setScale(2, RoundingMode.HALF_UP);
                String doctorName = result.getDoctorName() != null ? result.getDoctorName() : "医生";
                String scheduleTime = "就诊时段";
                String refundRatePercent = refundRate.multiply(BigDecimal.valueOf(100)).toString();
                
                messageService.sendRefundSuccessMessage(
                    payment.getPatientId(), 
                    doctorName, 
                    scheduleTime, 
                    refundAmount.toString(), 
                    refundRatePercent
                );
            } catch (Exception e) {
                log.warn("Failed to send refund success message: {}", e.getMessage());
            }
        }

        return result;
    }
    
    /**
     * 计算退款比例
     * @param now 当前时间
     * @param scheduleStartTime 排班开始时间
     * @return 退款比例 (0-1)
     */
    private BigDecimal calculateRefundRate(LocalDateTime now, LocalDateTime scheduleStartTime) {
        // 计算时间差（小时）
        Duration duration = Duration.between(now, scheduleStartTime);
        BigDecimal hoursBefore = BigDecimal.valueOf(duration.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        
        if (hoursBefore.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO; // 已过期
        }
        
        // 从数据库查询退款比例
        BigDecimal rate = refundMapper.getRefundRateByHours(hoursBefore);
        return rate != null ? rate : BigDecimal.ZERO;
    }
}
