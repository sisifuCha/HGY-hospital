package com.example.pojo.dto;
import lombok.Data;
@Data
public class EmailVerificationSendRequest {
    private String email;
    /**
     * 业务场景：REGISTER（默认）
     */
    private String scene;
}