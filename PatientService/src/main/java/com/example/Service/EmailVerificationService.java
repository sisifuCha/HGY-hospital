package com.example.Service;

import com.example.conmon.result.Result;
import com.example.pojo.dto.EmailVerificationSendRequest;
import com.example.pojo.dto.EmailVerificationSendResponse;
import com.example.pojo.dto.EmailVerificationVerifyRequest;
import com.example.pojo.dto.EmailVerificationVerifyResponse;

/**
 * 邮箱验证码服务接口
 */
public interface EmailVerificationService {

    /**
     * 发送验证码
     */
    Result<EmailVerificationSendResponse> sendCode(EmailVerificationSendRequest request);

    /**
     * 验证验证码
     */
    Result<EmailVerificationVerifyResponse> verifyCode(EmailVerificationVerifyRequest request);
}
