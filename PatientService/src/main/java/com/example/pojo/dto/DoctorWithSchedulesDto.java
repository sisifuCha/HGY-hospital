package com.example.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class DoctorWithSchedulesDto {
    private String id;
    private String name;
    private String title; // 医生职称
    private List<ScheduleDto> schedules;
}

