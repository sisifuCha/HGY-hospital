package com.example.pojo.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RegistrationDto {
    private String patientId;
    private String scheduleRecordId;
    private String registerTime;
    private boolean status;
    private String paymentId;  // 自动生成的订单ID
    private BigDecimal amount;  // 应付金额
}


