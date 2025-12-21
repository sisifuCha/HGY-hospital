package com.example.Controller;

import com.example.Service.EmailVerificationService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.EmailVerificationSendRequest;
import com.example.pojo.dto.EmailVerificationSendResponse;
import com.example.pojo.dto.EmailVerificationVerifyRequest;
import com.example.pojo.dto.EmailVerificationVerifyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email-verification")
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    public Result<EmailVerificationSendResponse> send(@RequestBody EmailVerificationSendRequest request) {
        return emailVerificationService.sendCode(request);
    }

    @PostMapping("/verify")
    public Result<EmailVerificationVerifyResponse> verify(@RequestBody EmailVerificationVerifyRequest request) {
        return emailVerificationService.verifyCode(request);
    }
}

