package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {

    private String userName;

    private String userId;

    private String userGender;

    private String userAccount;

    private String userEmail;

    private String userPassword;

    private String userPhone;

    private String doctorId;

    private String titleId;

    private String departmentId;

    private String doctorStatus;
}