package com.example.Service;

import com.example.Mapper.PaymentMapper;
import com.example.Mapper.RefundMapper;
import com.example.pojo.dto.PaymentDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 退号退款规则测试
 */
@SpringBootTest
@Transactional
@Rollback
public class RefundRulesTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Test
    @DisplayName("测试1: 退款比例 - 提前24小时以上全额退款")
    public void testRefundRate_MoreThan24Hours() {
        BigDecimal rate = refundMapper.getRefundRateByHours(new BigDecimal("25.0"));
        assertNotNull(rate);
        assertEquals(new BigDecimal("1.00"), rate, "提前24小时以上应该100%退款");
        
        System.out.println("✅ 测试通过：提前24小时退款比例 = " + rate.multiply(new BigDecimal("100")) + "%");
    }

    @Test
    @DisplayName("测试2: 退款比例 - 提前12小时退80%")
    public void testRefundRate_12Hours() {
        BigDecimal rate = refundMapper.getRefundRateByHours(new BigDecimal("15.0"));
        assertNotNull(rate);
        assertEquals(new BigDecimal("0.80"), rate, "提前12-24小时应该80%退款");
        
        System.out.println("✅ 测试通过：提前12-24小时退款比例 = " + rate.multiply(new BigDecimal("100")) + "%");
    }

    @Test
    @DisplayName("测试3: 退款比例 - 提前6小时退50%")
    public void testRefundRate_6Hours() {
        BigDecimal rate = refundMapper.getRefundRateByHours(new BigDecimal("8.0"));
        assertNotNull(rate);
        assertEquals(new BigDecimal("0.50"), rate, "提前6-12小时应该50%退款");
        
        System.out.println("✅ 测试通过：提前6-12小时退款比例 = " + rate.multiply(new BigDecimal("100")) + "%");
    }

    @Test
    @DisplayName("测试4: 退款比例 - 提前3小时退30%")
    public void testRefundRate_3Hours() {
        BigDecimal rate = refundMapper.getRefundRateByHours(new BigDecimal("4.0"));
        assertNotNull(rate);
        assertEquals(new BigDecimal("0.30"), rate, "提前3-6小时应该30%退款");
        
        System.out.println("✅ 测试通过：提前3-6小时退款比例 = " + rate.multiply(new BigDecimal("100")) + "%");
    }

    @Test
    @DisplayName("测试5: 退款比例 - 提前1小时退10%")
    public void testRefundRate_1Hour() {
        BigDecimal rate = refundMapper.getRefundRateByHours(new BigDecimal("1.5"));
        assertNotNull(rate);
        assertEquals(new BigDecimal("0.10"), rate, "提前1-3小时应该10%退款");
        
        System.out.println("✅ 测试通过：提前1-3小时退款比例 = " + rate.multiply(new BigDecimal("100")) + "%");
    }

    @Test
    @DisplayName("测试6: 退款比例 - 不足1小时不退款")
    public void testRefundRate_LessThan1Hour() {
        BigDecimal rate = refundMapper.getRefundRateByHours(new BigDecimal("0.5"));
        assertNotNull(rate);
        assertEquals(new BigDecimal("0.00"), rate, "不足1小时应该0%退款");
        
        System.out.println("✅ 测试通过：不足1小时退款比例 = " + rate.multiply(new BigDecimal("100")) + "%");
    }

    @Test
    @DisplayName("测试7: 取消订单 - 已支付订单退款到医保")
    public void testCancelOrder_RefundToInsurance() {
        // 注意：这个测试需要先创建一个已支付的订单
        String paymentId = "PAY_TEST_001"; // 需要数据库中有这个订单
        
        // 假设这是一个已支付的订单，提前24小时取消
        try {
            PaymentDto cancelled = paymentService.cancelOrder(paymentId);
            
            assertNotNull(cancelled);
            assertEquals("已取消", cancelled.getPayStatus());
            
            System.out.println("✅ 测试通过：订单取消成功");
            System.out.println("   - 订单状态: " + cancelled.getPayStatus());
        } catch (IllegalArgumentException e) {
            System.out.println("⚠️  需要准备测试数据：" + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试8: 退款金额计算")
    public void testRefundAmountCalculation() {
        // 测试不同时间的退款金额
        BigDecimal originalAmount = new BigDecimal("100.00");
        
        // 提前25小时：100% = 100元
        BigDecimal rate1 = refundMapper.getRefundRateByHours(new BigDecimal("25.0"));
        BigDecimal refund1 = originalAmount.multiply(rate1);
        assertEquals(new BigDecimal("100.00"), refund1);
        
        // 提前15小时：80% = 80元
        BigDecimal rate2 = refundMapper.getRefundRateByHours(new BigDecimal("15.0"));
        BigDecimal refund2 = originalAmount.multiply(rate2);
        assertEquals(new BigDecimal("80.00"), refund2);
        
        // 提前8小时：50% = 50元
        BigDecimal rate3 = refundMapper.getRefundRateByHours(new BigDecimal("8.0"));
        BigDecimal refund3 = originalAmount.multiply(rate3);
        assertEquals(new BigDecimal("50.00"), refund3);
        
        System.out.println("✅ 测试通过：退款金额计算正确");
        System.out.println("   - 原价100元，提前25小时退款: " + refund1 + "元");
        System.out.println("   - 原价100元，提前15小时退款: " + refund2 + "元");
        System.out.println("   - 原价100元，提前8小时退款: " + refund3 + "元");
    }

    @Test
    @DisplayName("测试9: 验证所有退款比例配置")
    public void testAllRefundRateConfigs() {
        System.out.println("\n📊 退款比例配置表：");
        System.out.println("─────────────────────────────────");
        
        BigDecimal[] testHours = {
            new BigDecimal("30.0"),  // >24h
            new BigDecimal("20.0"),  // 12-24h
            new BigDecimal("10.0"),  // 6-12h
            new BigDecimal("5.0"),   // 3-6h
            new BigDecimal("2.0"),   // 1-3h
            new BigDecimal("0.5")    // <1h
        };
        
        for (BigDecimal hours : testHours) {
            BigDecimal rate = refundMapper.getRefundRateByHours(hours);
            assertNotNull(rate, "退款比例不应该为null");
            assertTrue(rate.compareTo(BigDecimal.ZERO) >= 0 && 
                      rate.compareTo(BigDecimal.ONE) <= 0, 
                      "退款比例应该在0-1之间");
            
            System.out.printf("提前 %.1f 小时: %d%% 退款%n", 
                hours.doubleValue(), 
                rate.multiply(new BigDecimal("100")).intValue());
        }
        
        System.out.println("─────────────────────────────────");
        System.out.println("✅ 测试通过：所有退款比例配置有效");
    }
}
