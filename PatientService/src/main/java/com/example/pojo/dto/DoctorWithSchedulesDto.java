package com.example.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class DoctorWithSchedulesDto {
    private String doctorId;
    private String doctorName;
    private String doctorTitle; // 医生职称
    private String clinicId;
    private String clinicName;
    private String specialty;
    private String details; // 简介/履历
    private List<ScheduleDto> schedules;
}
