package com.example.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

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
        mailSender.send(message);
    }

    private String buildBody(String scene, String code, long expireSeconds) {
        String message="";
        if(scene.equals("REGISTER")) {
            message="注册";
        }else{
            message="找回密码";
        }
        return "您正在进行「" + message + "」操作，验证码：" + code
                + "\n验证码有效期：" + expireSeconds + " 秒。"
                + "\n如非本人操作，请忽略此邮件。";
    }
}

