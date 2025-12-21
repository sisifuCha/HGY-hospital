package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailVerificationSendResponse {
    private String email;
    private String scene;
    private long expireSeconds;
}

