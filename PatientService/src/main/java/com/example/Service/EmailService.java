package com.example.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送激活验证码邮件
     * @param toEmail 收件人邮箱
     * @param userName 用户姓名
     * @param verificationCode 验证码
     */
    public void sendActivationCode(String toEmail, String userName, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("医院挂号系统 - 账户激活验证码");
        
        String content = String.format(
            "尊敬的 %s 用户：\n\n" +
            "您好！感谢您注册医院挂号系统。\n\n" +
            "您的账户激活验证码为：%s\n\n" +
            "验证码有效期为30分钟，请尽快完成激活。\n\n" +
            "如果这不是您的操作，请忽略此邮件。\n\n" +
            "此邮件由系统自动发送，请勿回复。\n\n" +
            "祝您使用愉快！\n" +
            "医院挂号系统",
            userName, verificationCode
        );
        
        message.setText(content);
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }
}
