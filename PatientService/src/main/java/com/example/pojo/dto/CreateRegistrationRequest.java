package com.example.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRegistrationRequest {
    @NotBlank(message = "patientId不能为空")
    private String patientId;
    @NotBlank(message = "scheduleRecordId不能为空")
    private String scheduleRecordId;
    private boolean confirm = true;
}
