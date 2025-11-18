package com.example.pojo.vo;

import lombok.Data;

@Data
public class PatientProfileVo {
    private String patientId;
    private String name;
    private String gender;
    private String birthDate;
    private String phone;
    private String address;
    private String medicalHistory;
    private String allergies;
}

