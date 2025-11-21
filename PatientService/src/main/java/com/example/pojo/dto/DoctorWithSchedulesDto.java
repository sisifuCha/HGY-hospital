package com.example.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class DoctorWithSchedulesDto {
    private String doctorId;
    private String doctorName;
    private String doctorTitle; // 医生职称
    private String specialty;
    private List<ScheduleDto> schedules;
}

