package com.example.Controller;

import com.example.Service.PaymentService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.CreatePaymentRequest;
import com.example.pojo.dto.PaymentDto;
import com.example.pojo.dto.PaymentQuoteDto;
import com.example.security.Authz;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 获取患者的所有订单列表
     * @param patientId 患者ID
     * @return 订单列表
     */
    @GetMapping
    public Result<?> getPayments(@RequestParam String patientId) {
        Authz.assertPatient(patientId);
        try {
            List<PaymentDto> payments = paymentService.getPaymentsByPatient(patientId);
            Map<String, Object> response = new HashMap<>();
            response.put("total", payments.size());
            response.put("payments", payments);
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail(500, "查询订单列表失败：" + e.getMessage());
        }
    }

    /**
     * 查看指定订单详情
     * @param paymentId 订单ID
     * @return 订单详情
     */
    @GetMapping("/{paymentId}")
    public Result<?> getPayment(@PathVariable String paymentId) {
        try {
            PaymentDto payment = paymentService.getPaymentById(paymentId);
            return Result.success(payment);
        } catch (IllegalArgumentException e) {
            return Result.fail(404, e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "查询订单失败：" + e.getMessage());
        }
    }

    /**
     * 支付订单（模拟支付并扣除医保余额）
     * @param paymentId 订单ID
     * @return 支付后的订单信息
     */
    @PostMapping("/{paymentId}/pay")
    public Result<?> payOrder(@PathVariable String paymentId) {
        try {
            PaymentDto payment = paymentService.payOrder(paymentId);
            return Result.success(payment);
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "支付失败：" + e.getMessage());
        }
    }

    /**
     * 取消订单
     * @param paymentId 订单ID
     * @return 取消后的订单信息
     */
    @DeleteMapping("/{paymentId}")
    public Result<?> cancelOrder(@PathVariable String paymentId) {
        try {
            PaymentDto payment = paymentService.cancelOrder(paymentId);
            return Result.success(payment);
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "取消订单失败：" + e.getMessage());
        }
    }

    /**
     * 支付试算（报销优惠）：不落库，仅用于支付页展示
     */
    @GetMapping("/quote")
    public Result<?> getPaymentQuote(@RequestParam String patientId, @RequestParam String scheduleRecordId) {
        Authz.assertPatient(patientId);
        try {
            PaymentQuoteDto quote = paymentService.getPaymentQuote(patientId, scheduleRecordId);
            return Result.success(quote);
        } catch (IllegalArgumentException e) {
            // 按文档：排班/患者不存在等
            String msg = e.getMessage();
            if (msg != null && (msg.contains("排班") || msg.contains("患者"))) {
                return Result.fail(404, msg);
            }
            return Result.fail(400, msg);
        } catch (Exception e) {
            return Result.fail(500, "支付试算失败：" + e.getMessage());
        }
    }
}
