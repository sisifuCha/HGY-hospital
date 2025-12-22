package com.example.Service;

import com.example.Mapper.PaymentMapper;
import com.example.Mapper.RegistrationMapper;
import com.example.conmon.exception.CreateFailedException;
import com.example.pojo.dto.PaymentDto;
import com.example.pojo.dto.PaymentQuoteDto;
import com.example.pojo.entity.MedicalInsurance;
import com.example.pojo.entity.PayRecord;
import com.example.pojo.entity.ReimburseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

        return paymentMapper.findPaymentById(paymentId);
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

        // 2. 更新支付状态为已取消
        int updated = paymentMapper.updatePaymentStatus(paymentId, "已取消");
        if (updated == 0) {
            throw new CreateFailedException();
        }

        // 3. 更新挂号记录状态为已取消
        int regUpdated = registrationMapper.updateRegistrationStatusToCanceled(payment.getPatientId(), payment.getSchId());
        if (regUpdated == 0) {
            log.warn("Failed to update registration status for cancelled payment: {}", paymentId);
        }

        // 4. 如果订单已支付，需要回补号源
        if ("已支付".equals(payment.getPayStatus())) {
            registrationMapper.incrementScheduleLeftSource(payment.getSchId());
            log.info("Source restored for cancelled paid order: {}", paymentId);
        }

        log.info("Order cancelled: {}", paymentId);

        return paymentMapper.findPaymentById(paymentId);
    }

    @Override
    public PaymentQuoteDto getPaymentQuote(String patientId, String scheduleRecordId) {
        PaymentQuoteDto quote = paymentMapper.getPaymentQuote(patientId, scheduleRecordId);
        if (quote == null || quote.getOriAmount() == null) {
            throw new IllegalArgumentException("排班不存在或无法获取挂号费");
        }

        BigDecimal oriAmount = quote.getOriAmount();

        BigDecimal reimbursePercent = quote.getReimbursePercent();
        if (reimbursePercent == null) {
            reimbursePercent = BigDecimal.ZERO;
            quote.setReimbursePercent(reimbursePercent);
        }

        // askPayAmount = ori * (1 - percent/100)
        BigDecimal reimburseFactor = BigDecimal.ONE.subtract(
                reimbursePercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        BigDecimal askPayAmount = oriAmount.multiply(reimburseFactor).setScale(2, RoundingMode.HALF_UP);
        quote.setAskPayAmount(askPayAmount);

        BigDecimal reimburseAmount = oriAmount.subtract(askPayAmount).setScale(2, RoundingMode.HALF_UP);
        quote.setReimburseAmount(reimburseAmount);

        // 医保余额是否足够（未绑定医保则视为不够，交给前端提示）
        BigDecimal medicalOverage = quote.getMedicalInsuranceOverage();
        if (medicalOverage == null) {
            quote.setInsuranceEnough(false);
        } else {
            quote.setInsuranceEnough(medicalOverage.compareTo(askPayAmount) >= 0);
        }

        return quote;
    }
}
