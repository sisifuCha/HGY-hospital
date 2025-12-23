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

/**
 * 邮箱验证码服务实现
 */
@Slf4j
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationCodeStore codeStore;
    private final EmailSender emailSender;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 验证码有效期（秒），默认 5 分钟
     */
    @Value("${patient.emailVerification.expireSeconds:300}")
    private long expireSeconds;

    /**
     * 发送冷却时间（秒），默认 60 秒内不能重复发送
     */
    @Value("${patient.emailVerification.sendCooldownSeconds:60}")
    private long sendCooldownSeconds;

    /**
     * 是否真正发送邮件（开发环境可设为 false，验证码会打印到控制台）
     */
    @Value("${patient.emailVerification.realSendEnabled:true}")
    private boolean realSendEnabled;

    public EmailVerificationServiceImpl(EmailVerificationCodeStore codeStore, EmailSender emailSender) {
        this.codeStore = codeStore;
        this.emailSender = emailSender;
    }

    @Override
    public Result<EmailVerificationSendResponse> sendCode(EmailVerificationSendRequest request) {
        // 1. 参数校验
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            return Result.fail(400, "邮箱不能为空");
        }

        String email = request.getEmail().trim();
        if (!isValidEmail(email)) {
            return Result.fail(400, "邮箱格式不正确");
        }

        // 2. 处理 scene，默认 REGISTER
        String scene = (request.getScene() == null || request.getScene().isBlank())
                ? "REGISTER"
                : request.getScene().trim().toUpperCase();

        String key = EmailVerificationStore.buildKey(email, scene);

        // 3. 发送冷却检查
        if (!codeStore.tryAcquireCooldown(key, sendCooldownSeconds)) {
            return Result.fail(429, "发送过于频繁，请 " + sendCooldownSeconds + " 秒后再试");
        }

        // 4. 生成 6 位验证码
        String code = generateCode();
        codeStore.save(key, code, expireSeconds);

        log.info("验证码已生成 | email={}, scene={}, code={}", email, scene, code);

        // 5. 发送邮件
        if (realSendEnabled) {
            try {
                emailSender.sendVerificationCode(email, scene, code, expireSeconds);
            } catch (Exception e) {
                log.error("邮件发送失败 | email={}, scene={}, error={}", email, scene, e.getMessage(), e);
                // 发送失败，删除已保存的验证码
                codeStore.delete(key);
                return Result.fail(500, "验证码发送失败: " + e.getMessage());
            }
        } else {
            log.warn("【开发模式】验证码未真正发送 | email={}, scene={}, code={}", email, scene, code);
        }

        return Result.success(new EmailVerificationSendResponse(email, scene, expireSeconds));
    }

    @Override
    public Result<EmailVerificationVerifyResponse> verifyCode(EmailVerificationVerifyRequest request) {
        // 1. 参数校验
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

        if (request.getCode() == null || request.getCode().isBlank()) {
            return Result.fail(400, "验证码不能为空");
        }

        String code = request.getCode().trim();
        if (!code.matches("\\d{6}")) {
            return Result.fail(400, "验证码格式不正确，应为6位数字");
        }

        // 2. 处理 scene
        String scene = (request.getScene() == null || request.getScene().isBlank())
                ? "REGISTER"
                : request.getScene().trim().toUpperCase();

        String key = EmailVerificationStore.buildKey(email, scene);

        // 3. 获取存储的验证码
        String savedCode = codeStore.getCode(key);
        if (savedCode == null) {
            log.warn("验证码不存在或已过期 | email={}, scene={}", email, scene);
            return Result.fail(410, "验证码已过期或不存在，请重新获取");
        }

        // 4. 验证码比对
        if (!savedCode.equals(code)) {
            log.warn("验证码错误 | email={}, scene={}, input={}, expected={}", email, scene, code, savedCode);
            return Result.fail(400, "验证码错误");
        }

        // 5. 验证成功，删除验证码（一次性使用）
        codeStore.delete(key);
        log.info("验证码验证成功 | email={}, scene={}", email, scene);

        return Result.success(new EmailVerificationVerifyResponse(true, email, scene));
    }

    /**
     * 生成 6 位随机数字验证码
     */
    private String generateCode() {
        int n = RANDOM.nextInt(1_000_000);
        return String.format("%06d", n);
    }

    /**
     * 简单的邮箱格式校验
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
