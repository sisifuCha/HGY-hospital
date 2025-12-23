package com.example.pojo.dto;

import lombok.Data;

@Data
public class EmailVerificationVerifyRequest {
    private String email;
    private String scene;
    private String code;
}

