package com.example.pojo.dto;

import lombok.Data;

/**
 * 登录请求的数据传输对象
 */
@Data
public class LoginRequest {
    private String account;
    private String password;
}

