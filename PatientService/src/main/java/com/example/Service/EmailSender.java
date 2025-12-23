package com.example.Service;

/**
 * 邮件发送接口
 */
public interface EmailSender {

    /**
     * 发送验证码邮件
     *
     * @param toEmail       收件人邮箱
     * @param scene         业务场景（如 REGISTER、RESET_PASSWORD）
     * @param code          验证码
     * @param expireSeconds 验证码有效期（秒）
     */
    void sendVerificationCode(String toEmail, String scene, String code, long expireSeconds);
}
