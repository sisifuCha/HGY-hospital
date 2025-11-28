package com.example.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class RegistrationQueryDto {
    private String patientId;
    private String date;
    private String fromDate;
    private String toDate;
    private String status;
    private List<String> statusList;
    private Integer page = 1;
    private Integer pageSize = 20;
}
