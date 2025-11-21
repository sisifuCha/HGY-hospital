package com.example.pojo.dto;

import lombok.Data;

@Data
public class RegistrationDto {
    private String patientId;
    private String scheduleRecordId;
    private String registerTime;
    private String status;

    // Optional enrichments for list/get
    private String doctorId;
    private String departmentId;
    private String scheduleDate;
    private String timePeriodName;
}

