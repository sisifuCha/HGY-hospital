package com.example.pojo.dto;

import lombok.Data;

@Data
public class PatientProfileDto {
    private String name;
    private String gender;
    private String birthDate;
    private String phone;
    private String address;
    private String medicalHistory;
    private String allergies;
}

