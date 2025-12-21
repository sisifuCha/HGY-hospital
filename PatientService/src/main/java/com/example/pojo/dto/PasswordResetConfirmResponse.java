package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetConfirmResponse {
    private boolean reset;
    private String email;
}

