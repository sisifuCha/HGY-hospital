package com.example.pojo.dto;

import lombok.Data;

@Data
public class PasswordResetConfirmRequest {
    private String email;
    private String code;
    private String newPassword;
    private String confirmPassword;
}

