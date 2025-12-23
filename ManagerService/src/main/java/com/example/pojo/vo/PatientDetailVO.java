package com.example.pojo.vo;

import lombok.Data;

import java.util.Date;

@Data
public class PatientDetailVO {
    // user和patient表公共字段
    private String id;
    
    // user表字段
    private String name;
    private String sex;
    private String account;
    private String email;
    private String pass;
    private String phone_num;
    private String user_type;
    
    // patient字段
    private String birth;
    private String id_num;
    private String medical_insuranceid;
    private String reimburse_id;
    private String status;
}