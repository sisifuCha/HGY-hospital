package com.example.pojo.entity;

import lombok.Data;

@Data
public class Doctor {
    private String id;
    private String name; // From user table
    private String docTitleId;
    private String title; // From title_number_source table
    private String status;
    private String clinicId;
    private String details;
    private String specialty;
    private String departId;
}

