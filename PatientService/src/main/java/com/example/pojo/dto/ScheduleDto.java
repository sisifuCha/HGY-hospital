package com.example.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class ScheduleDto {
    private String scheduleId; // 排班记录ID (sch_id)
    private String startTime;
    private String endTime;
    private String timePeriodName; // e.g., "上午"
    private Integer leftSourceCount; // 剩余号源
    private BigDecimal registrationFee;
}

