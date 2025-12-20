package com.example.pojo.vo;

import lombok.Data;

/**
 * 患者详情：聚合 user + patient 档案信息。
 * phone 字段为“展示用电话”：优先 patient.phone，其次 user.phone_num。
 */
@Data
public class PatientDetailVo {
    private String patientId;

    // user 表字段
    private String account;
    private String name;
    private String gender;
    private String email;

    /**
     * 展示用电话：COALESCE(patient.phone, user.phone_num)
     */
    private String phone;

    // patient 档案字段
    private String birthday;
    private String address;
    private String medicalHistory;
    private String allergies;
}

