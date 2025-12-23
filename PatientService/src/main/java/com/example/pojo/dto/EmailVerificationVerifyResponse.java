package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证邮箱验证码响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationVerifyResponse {
    /**
     * 验证是否成功
     */
    private boolean verified;

    /**
     * 验证的邮箱
     */
    private String email;

    /**
     * 业务场景
     */
    private String scene;
}
