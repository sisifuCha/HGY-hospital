package com.example.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 导诊请求
 */
@Data
public class TriageRequest {
    
    @NotBlank(message = "患者ID不能为空")
    private String patientId;
    
    @Size(min = 1, message = "症状列表不能为空")
    private List<String> symptoms;
    
    private Integer durationDays;
    private Double temperature;
    private List<String> allergies;
    private List<String> history;
    private List<String> attachments;
    private String mode; // rule, ml, hybrid
}
