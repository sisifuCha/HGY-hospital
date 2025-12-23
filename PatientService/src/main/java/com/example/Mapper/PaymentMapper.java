package com.example.Mapper;

import com.example.pojo.dto.PaymentDto;
import com.example.pojo.entity.MedicalInsurance;
import com.example.pojo.entity.PayRecord;
import com.example.pojo.entity.ReimburseType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface PaymentMapper {
    // 根据患者ID和排班ID查询报销信息
    ReimburseType getReimburseTypeByPatient(@Param("patientId") String patientId);
    
    // 根据医生职称查询挂号费
    BigDecimal getRegistrationFeeBySchedule(@Param("scheduleRecordId") String scheduleRecordId);
    
    // 获取医生ID
    String getDoctorIdBySchedule(@Param("scheduleRecordId") String scheduleRecordId);
    
    // 插入支付记录
    int insertPayment(@Param("payment") PayRecord payment);
    
    // 查询患者的所有支付记录
    List<PaymentDto> findPaymentsByPatient(@Param("patientId") String patientId);
    
    // 根据ID查询支付记录详情
    PaymentDto findPaymentById(@Param("paymentId") String paymentId);
    
    // 更新支付状态
    int updatePaymentStatus(@Param("paymentId") String paymentId, @Param("status") String status);
    
    // 查询医保余额
    MedicalInsurance getMedicalInsurance(@Param("medicalInsuranceId") String medicalInsuranceId);
    
    // 扣减医保余额
    int deductMedicalInsurance(@Param("medicalInsuranceId") String medicalInsuranceId, @Param("amount") BigDecimal amount);
    
    // 根据患者ID和排班ID查询支付记录
    PayRecord findPaymentByPatientAndSchedule(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId);
    
    // 查询患者医保ID
    String getPatientMedicalInsuranceId(@Param("patientId") String patientId);
    
    // 根据支付记录ID查询患者ID和排班ID
    PayRecord findPaymentInfoById(@Param("paymentId") String paymentId);
    
    // 查询患者医保账户ID
    String findMedicalInsuranceIdByPatient(@Param("patientId") String patientId);
    
    // 退款到医保账户
    int refundToMedicalInsurance(@Param("medicalInsuranceId") String medicalInsuranceId, @Param("amount") BigDecimal amount);

    // 查询接近超时的待支付订单（用于发送提醒，1分30秒到1分31秒之间）
    List<PayRecord> findPendingPaymentsForTimeout();

    // 查询已超时的待支付订单（超过2分钟）
    List<PayRecord> findTimeoutPendingPayments();
}
