package com.example.Mapper;

import com.example.pojo.dto.PaymentDto;
import com.example.pojo.dto.PaymentQuoteDto;
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

    /**
     * 支付试算：查询患者在指定排班下的挂号费、报销比例、医保余额（仅查询，不落库）
     */
    PaymentQuoteDto getPaymentQuote(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId);

    /**
     * 查询超时未支付的订单（pay_status=待支付 且 pay_time <= now - timeoutMinutes）
     */
    java.util.List<PayRecord> findExpiredUnpaidPayments(@Param("timeoutMinutes") int timeoutMinutes);

    /**
     * 将订单置为已取消（幂等：仅当当前仍为待支付时更新）
     */
    int cancelPaymentIfUnpaid(@Param("paymentId") String paymentId);
}
