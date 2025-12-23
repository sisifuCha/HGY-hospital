package com.example.Service;

import com.example.conmon.result.Result;
import com.example.pojo.dto.EmailVerificationSendRequest;
import com.example.pojo.dto.EmailVerificationSendResponse;
import com.example.pojo.dto.EmailVerificationVerifyRequest;
import com.example.pojo.dto.EmailVerificationVerifyResponse;
import com.example.utils.EmailVerificationCodeStore;
import com.example.utils.EmailVerificationStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationCodeStore codeStore;
    private final EmailSender emailSender;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${patient.emailVerification.expireSeconds:300}")
    private long expireSeconds;

    @Value("${patient.emailVerification.sendCooldownSeconds:60}")
    private long sendCooldownSeconds;

    @Value("${patient.emailVerification.realSendEnabled:false}")
    private boolean realSendEnabled;

    public EmailVerificationServiceImpl(EmailVerificationCodeStore codeStore, EmailSender emailSender) {
        this.codeStore = codeStore;
        this.emailSender = emailSender;
    }

    @Override
    public Result<EmailVerificationSendResponse> sendCode(EmailVerificationSendRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            return Result.fail(400, "邮箱不能为空");
        }
        String email = request.getEmail().trim();
        if (!isValidEmail(email)) {
            return Result.fail(400, "邮箱格式不正确");
        }

        String scene = (request.getScene() == null || request.getScene().isBlank()) ? "REGISTER" : request.getScene().trim().toUpperCase();
        String key = EmailVerificationStore.buildKey(email, scene);

        // 发送冷却：cooldownSeconds 内不允许重复发送
        if (!codeStore.tryAcquireCooldown(key, sendCooldownSeconds)) {
            return Result.fail(429, "发送过于频繁，请稍后再试（冷却" + sendCooldownSeconds + "秒）");
        }

        String code = random6Digits();
        codeStore.save(key, code, expireSeconds);
        System.out.println("邮箱发送：验证码生成完毕"+code.toString());
        // SMTP 真发送：发送失败则删除已保存的验证码
        if (realSendEnabled) {
            try {
                emailSender.sendVerificationCode(email, scene, code, expireSeconds);
            } catch (Exception e) {
                log.error("邮件发送失败 | email={}, scene={}, 异常类型={}, 异常信息={}",
                        email, scene, e.getClass().getName(), e.getMessage(), e);
                codeStore.delete(key);
                return Result.fail(500, "验证码发送失败: " + e.getMessage());
            }
        }

        return Result.success(new EmailVerificationSendResponse(email, scene, expireSeconds));
    }

    @Override
    public Result<EmailVerificationVerifyResponse> verifyCode(EmailVerificationVerifyRequest request) {
        if (request == null) {
            return Result.fail(400, "请求参数不能为空");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return Result.fail(400, "邮箱不能为空");
        }
        String email = request.getEmail().trim();
        if (!isValidEmail(email)) {
            return Result.fail(400, "邮箱格式不正确");
        }

        String scene = (request.getScene() == null || request.getScene().isBlank()) ? "REGISTER" : request.getScene().trim().toUpperCase();
        String code = (request.getCode() == null) ? "" : request.getCode().trim();
        if (!code.matches("\\d{6}")) {
            return Result.fail(400, "验证码格式不正确");
        }

        String key = EmailVerificationStore.buildKey(email, scene);
        String savedCode = codeStore.getCode(key);
        if (savedCode == null) {
            return Result.fail(410, "验证码已过期或不存在");
        }

        if (!savedCode.equals(code)) {
            return Result.fail(400, "验证码错误");
        }

        // 校验成功：立刻失效
        codeStore.delete(key);
        return Result.success(new EmailVerificationVerifyResponse(true, email, scene));
    }

    private static String random6Digits() {
        int n = RANDOM.nextInt(1_000_000);
        return String.format("%06d", n);
    }

    private static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
