package com.example.pojo.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class ScheduleDto {
    private String id; // 排班记录ID (sch_id)
    private LocalTime startTime;
    private LocalTime endTime;
    private String timePeriodName; // e.g., "上午"
    private Integer leftSourceCount; // 剩余号源
}

