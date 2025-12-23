package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送邮箱验证码响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationSendResponse {
    /**
     * 发送到的邮箱
     */
    private String email;

    /**
     * 业务场景
     */
    private String scene;

    /**
     * 验证码有效期（秒）
     */
    private long expireSeconds;
}
