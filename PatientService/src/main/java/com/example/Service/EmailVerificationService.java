package com.example.Service;

import com.example.conmon.result.Result;
import com.example.pojo.dto.EmailVerificationSendRequest;
import com.example.pojo.dto.EmailVerificationSendResponse;
import com.example.pojo.dto.EmailVerificationVerifyRequest;
import com.example.pojo.dto.EmailVerificationVerifyResponse;

public interface EmailVerificationService {
    Result<EmailVerificationSendResponse> sendCode(EmailVerificationSendRequest request);

    Result<EmailVerificationVerifyResponse> verifyCode(EmailVerificationVerifyRequest request);
}

