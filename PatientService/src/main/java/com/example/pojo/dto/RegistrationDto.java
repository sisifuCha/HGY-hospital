package com.example.pojo.dto;

import lombok.Data;

@Data
public class RegistrationDto {
    private String patientId;
    private String scheduleRecordId;
    private String registerTime;
    private boolean status;
}


