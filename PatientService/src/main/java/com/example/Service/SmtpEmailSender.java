package com.example.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 基于 SMTP 的邮件发送实现
 */
@Slf4j
@Component
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${patient.emailVerification.from:}")
    private String from;

    public SmtpEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationCode(String toEmail, String scene, String code, long expireSeconds) {
        String realFrom = (from == null || from.isBlank()) ? mailUsername : from;
        if (realFrom == null || realFrom.isBlank()) {
            throw new IllegalStateException("未配置发件人账号：请设置 spring.mail.username 或 patient.emailVerification.from");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(realFrom);
        message.setTo(toEmail);
        message.setSubject("【红果园校医院】邮箱验证码");
        message.setText(buildBody(scene, code, expireSeconds));

        log.info("发送验证码邮件 | to={}, scene={}", toEmail, scene);
        mailSender.send(message);
        log.info("验证码邮件发送成功 | to={}, scene={}", toEmail, scene);
    }

    private String buildBody(String scene, String code, long expireSeconds) {
        String sceneText = getSceneText(scene);
        return "您正在进行「" + sceneText + "」操作，验证码：" + code
                + "\n验证码有效期：" + (expireSeconds / 60) + " 分钟。"
                + "\n如非本人操作，请忽略此邮件。";
    }

    private String getSceneText(String scene) {
        if (scene == null) {
            return "验证";
        }
        return switch (scene.toUpperCase()) {
            case "REGISTER" -> "注册账号";
            case "RESET_PASSWORD" -> "找回密码";
            case "BIND_EMAIL" -> "绑定邮箱";
            default -> scene;
        };
    }
}
