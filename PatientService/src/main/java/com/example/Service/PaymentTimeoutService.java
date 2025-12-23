package com.example.Service;

/**
 * 支付超时处理：
 * - 扫描待支付超时订单（默认 15 分钟）
 * - 自动取消订单与挂号
 * - 回补号源或候补自动转正并生成新订单
 */
public interface PaymentTimeoutService {

    /**
     * 执行一次超时扫描与处理。
     * @return 本次处理的超时订单数量
     */
    int processExpiredUnpaidPayments();
}

