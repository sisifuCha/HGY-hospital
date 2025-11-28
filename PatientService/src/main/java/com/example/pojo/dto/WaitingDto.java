package com.example.pojo.dto;

import lombok.Data;

@Data
public class WaitingDto {
    private String waitingId;
    private String patientId;
    private String scheduleRecordId;
    private String applyTime;
    private String status;
    private Integer position;
    private Integer limitCount; // remaining today count
}

