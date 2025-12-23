package com.example.pojo.dto;

import lombok.Data;

/**
 * 发送邮箱验证码请求
 */
@Data
public class EmailVerificationSendRequest {
    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 业务场景：REGISTER（注册）、RESET_PASSWORD（找回密码）等
     * 不传默认为 REGISTER
     */
    private String scene;
}
