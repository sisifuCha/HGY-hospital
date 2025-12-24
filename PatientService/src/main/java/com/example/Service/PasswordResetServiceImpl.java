package com.example.Service;

import com.example.Mapper.UserMapper;
import com.example.conmon.result.Result;
import com.example.pojo.dto.PasswordResetConfirmRequest;
import com.example.pojo.dto.PasswordResetConfirmResponse;
import com.example.pojo.dto.PasswordResetSendRequest;
import com.example.pojo.dto.PasswordResetSendResponse;
import com.example.pojo.entity.User;
import com.example.utils.EmailVerificationCodeStore;
import com.example.utils.EmailVerificationStore;
import com.example.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final String SCENE_RESET_PASSWORD = "RESET_PASSWORD";

    private final UserMapper userMapper;
    private final EmailVerificationCodeStore codeStore;
    private final EmailVerificationService emailVerificationService;
    private final PasswordUtil passwordUtil;

    @Value("${patient.emailVerification.expireSeconds:300}")
    private long expireSeconds;

    public PasswordResetServiceImpl(UserMapper userMapper,
                                   EmailVerificationCodeStore codeStore,
                                   EmailVerificationService emailVerificationService,
                                   PasswordUtil passwordUtil) {
        this.userMapper = userMapper;
        this.codeStore = codeStore;
        this.emailVerificationService = emailVerificationService;
        this.passwordUtil = passwordUtil;
    }

    @Override
    public Result<PasswordResetSendResponse> sendResetCode(PasswordResetSendRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            return Result.fail(400, "邮箱不能为空");
        }
        String email = request.getEmail().trim();
        if (!isValidEmail(email)) {
            return Result.fail(400, "邮箱格式不正确");
        }

        // 校验邮箱是否已注册
        User user = userMapper.findByEmail(email);
        if (user == null) {
            return Result.fail(404, "该邮箱未注册");
        }

        // 复用已有模块：发送验证码（scene=RESET_PASSWORD）
        var sendReq = new com.example.pojo.dto.EmailVerificationSendRequest();
        sendReq.setEmail(email);
        sendReq.setScene(SCENE_RESET_PASSWORD);
        Result<com.example.pojo.dto.EmailVerificationSendResponse> sendRes = emailVerificationService.sendCode(sendReq);
        if (sendRes.getCode() != 200) {
            return Result.fail(sendRes.getCode(), sendRes.getMsg());
        }

        return Result.success(new PasswordResetSendResponse(email, SCENE_RESET_PASSWORD, expireSeconds));
    }

    @Override
    @Transactional
    public Result<PasswordResetConfirmResponse> confirmReset(PasswordResetConfirmRequest request) {
        if (request == null) {
            return Result.fail(400, "请求参数不能为空");
        }
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        if (email.isBlank() || !isValidEmail(email)) {
            return Result.fail(400, "邮箱格式不正确");
        }
        String code = request.getCode() == null ? "" : request.getCode().trim();
        if (!code.matches("\\d{6}")) {
            return Result.fail(400, "验证码格式不正确");
        }
        String newPassword = request.getNewPassword() == null ? "" : request.getNewPassword();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();
        if (newPassword.isBlank() || confirmPassword.isBlank()) {
            return Result.fail(400, "新密码不能为空");
        }
        if (!newPassword.equals(confirmPassword)) {
            return Result.fail(400, "两次密码不一致", new PasswordResetConfirmResponse(false, email));
        }

        User user = userMapper.findByEmail(email);
        if (user == null) {
            return Result.fail(404, "该邮箱未注册", new PasswordResetConfirmResponse(false, email));
        }

        String key = EmailVerificationStore.buildKey(email, SCENE_RESET_PASSWORD);
        String savedCode = codeStore.getCode(key);
        if (savedCode == null) {
            return Result.fail(410, "验证码已过期或不存在", new PasswordResetConfirmResponse(false, email));
        }
        if (!savedCode.equals(code)) {
            return Result.fail(400, "验证码错误", new PasswordResetConfirmResponse(false, email));
        }

        // 验证码正确：立刻作废
        codeStore.delete(key);

        // 加密新密码
        String encryptedPassword = passwordUtil.encryptPassword(newPassword);
        int updated = userMapper.updatePasswordByUserId(user.getUserId(), encryptedPassword);
        if (updated <= 0) {
            return Result.fail(500, "重置密码失败", new PasswordResetConfirmResponse(false, email));
        }

        return Result.success(new PasswordResetConfirmResponse(true, email));
    }

    private static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
