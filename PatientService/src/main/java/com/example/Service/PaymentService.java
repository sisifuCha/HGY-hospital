package com.example.Service;

import com.example.pojo.dto.PaymentDto;
import java.util.List;

public interface PaymentService {
    /**
     * 创建支付订单（基于已有挂号记录）
     */
    PaymentDto createPayment(String patientId, String scheduleRecordId);
    
    /**
     * 获取患者的所有订单列表
     */
    List<PaymentDto> getPaymentsByPatient(String patientId);
    
    /**
     * 查看指定订单详情
     */
    PaymentDto getPaymentById(String paymentId);
    
    /**
     * 支付订单（模拟支付并扣除医保余额）
     */
    PaymentDto payOrder(String paymentId);
    
    /**
     * 取消订单
     */
    PaymentDto cancelOrder(String paymentId);
}
