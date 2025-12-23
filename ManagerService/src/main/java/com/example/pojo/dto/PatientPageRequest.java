package com.example.pojo.dto;

import lombok.Data;

@Data
public class PatientPageRequest {
    private Integer pageNum;
    private String pageSize;
}