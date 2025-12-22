package com.example;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 测试专用：提供 JavaMailSender mock，避免 SpringBootTest 因未配置 SMTP 而无法创建 SmtpEmailSender。
 */
@TestConfiguration
public class TestMailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }
}

