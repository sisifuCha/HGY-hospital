package com.example.pojo.dto;

import lombok.Data;

import java.util.Date;

/**
 * 注册请求的数据传输对象
 */
@Data
public class RegisterRequest {
    // 从 User 类继承的字段
    private String userAccount;
    private String userPassword;
    private String userName;
    private String userGender;
    private String userEmail;
    private String userPhone;

    // RegisterRequest 特有的字段
    private Date birthday;
    private String identificationId;
}
