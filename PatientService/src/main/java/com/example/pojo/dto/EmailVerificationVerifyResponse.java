package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailVerificationVerifyResponse {
    private boolean verified;
    private String email;
    private String scene;
}

