package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetSendResponse {
    private String email;
    private String scene;
    private long expireSeconds;
}

