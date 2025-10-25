package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {

    private String userName;

    private Integer userId;

    private String userGender;

    private String userAccount;

    private String userEmail;

    private String userPassword;

    private String userPhone;

    private Integer doctorId;

    private Integer titleId;

    private Integer departmentId;

    private String doctorStatus;
}