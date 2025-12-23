package com.example.Controller;

import com.example.Service.EmailVerificationService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.EmailVerificationSendRequest;
import com.example.pojo.dto.EmailVerificationSendResponse;
import com.example.pojo.dto.EmailVerificationVerifyRequest;
import com.example.pojo.dto.EmailVerificationVerifyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮箱验证码接口
 */
@Tag(name = "邮箱验证码", description = "发送和验证邮箱验证码")
@RestController
@RequestMapping("/api/email-verification")
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    /**
     * 发送验证码
     * POST /api/email-verification/send
     * 请求体: {"email": "xxx@example.com", "scene": "REGISTER"}
     */
    @Operation(summary = "发送验证码", description = "向指定邮箱发送6位数字验证码")
    @PostMapping("/send")
    public Result<EmailVerificationSendResponse> send(@RequestBody EmailVerificationSendRequest request) {
        return emailVerificationService.sendCode(request);
    }

    /**
     * 验证验证码
     * POST /api/email-verification/verify
     * 请求体: {"email": "xxx@example.com", "code": "123456", "scene": "REGISTER"}
     */
    @Operation(summary = "验证验证码", description = "验证用户输入的验证码是否正确")
    @PostMapping("/verify")
    public Result<EmailVerificationVerifyResponse> verify(@RequestBody EmailVerificationVerifyRequest request) {
        return emailVerificationService.verifyCode(request);
    }
}
