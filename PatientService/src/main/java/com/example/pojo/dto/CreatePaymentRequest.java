package com.example.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePaymentRequest {
    @NotBlank(message = "患者ID不能为空")
    private String patientId;
    
    @NotBlank(message = "排班记录ID不能为空")
    private String scheduleRecordId;
}
