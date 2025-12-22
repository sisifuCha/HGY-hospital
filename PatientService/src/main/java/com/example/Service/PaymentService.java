package com.example.Service;

import com.example.pojo.dto.PaymentDto;
import com.example.pojo.dto.PaymentQuoteDto;
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

    /**
     * 支付试算：用于支付页展示报销比例、优惠金额、应付金额以及医保余额是否足够。
     * 不落库、不改变订单/挂号状态。
     */
    PaymentQuoteDto getPaymentQuote(String patientId, String scheduleRecordId);
}
