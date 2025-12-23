package com.example.Service;

public interface EmailSender {
    void sendVerificationCode(String toEmail, String scene, String code, long expireSeconds);
}

