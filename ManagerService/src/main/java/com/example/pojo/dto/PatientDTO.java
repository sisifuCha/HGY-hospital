package com.example.pojo.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PatientDTO {
    private String name;
    private String sex;
    private String phoneNum;
    private String email;
    private Date birth;
    private String idNum;
    private String medicalInsuranceId;
    private String reimburseId;
}