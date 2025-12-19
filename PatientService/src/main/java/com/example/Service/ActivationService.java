package com.example.Service;

import com.example.Mapper.PatientMapper;
import com.example.conmon.result.Result;
import com.example.pojo.dto.SendActivationCodeRequest;
import com.example.pojo.dto.VerifyActivationCodeRequest;
import com.example.pojo.entity.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账户激活服务
 */
@Service
@Slf4j
public class ActivationService {

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private EmailService emailService;

    /**
     * 发送激活验证码
     * 第一步：验证患者信息并发送验证码到邮箱
     */
    @Transactional(readOnly = true)
    public Result<String> sendActivationCode(SendActivationCodeRequest request) {
        // 1. 验证身份证号是否存在
        Patient patient = patientMapper.findByIdentificationId(request.getIdentificationId());
        if (patient == null) {
            return Result.error("未找到该身份证号对应的患者信息，请先注册");
        }

        // 2. 检查是否已经激活
        if (patient.getStatus() != null && patient.getStatus()) {
            return Result.error("该账户已经激活，无需重复激活");
        }

        // 3. 生成6位验证码
        String verificationCode = verificationCodeService.generateCode();
        log.info("为身份证号 {} 生成激活验证码: {}", 
                 request.getIdentificationId().replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2"), 
                 verificationCode);

        // 4. 保存验证码到Redis，有效期30分钟
        verificationCodeService.saveCode(request.getIdentificationId(), verificationCode);

        // 5. 发送邮件
        try {
            emailService.sendActivationCode(request.getEmail(), request.getUserName(), verificationCode);
            return Result.success("验证码已发送至您的邮箱，有效期为30分钟");
        } catch (Exception e) {
            log.error("发送激活验证码邮件失败", e);
            // 发送失败时删除已保存的验证码
            verificationCodeService.deleteCode(request.getIdentificationId());
            return Result.error("邮件发送失败，请检查邮箱地址是否正确或稍后重试");
        }
    }

    /**
     * 验证激活码并激活账户
     * 第二步：验证用户提交的验证码，成功则激活账户
     */
    @Transactional
    public Result<String> verifyAndActivate(VerifyActivationCodeRequest request) {
        // 1. 验证身份证号是否存在
        Patient patient = patientMapper.findByIdentificationId(request.getIdentificationId());
        if (patient == null) {
            return Result.error("未找到该身份证号对应的患者信息");
        }

        // 2. 检查是否已经激活
        if (patient.getStatus() != null && patient.getStatus()) {
            return Result.error("该账户已经激活，无需重复激活");
        }

        // 3. 验证验证码
        boolean isValid = verificationCodeService.verifyCode(
            request.getIdentificationId(), 
            request.getVerificationCode()
        );

        if (!isValid) {
            long remainingTime = verificationCodeService.getRemainingTime(request.getIdentificationId());
            if (remainingTime > 0) {
                return Result.error(String.format("验证码错误，请重新输入。验证码剩余有效时间：%d分钟", 
                                                  remainingTime / 60));
            } else {
                return Result.error("验证码已过期或不存在，请重新获取验证码");
            }
        }

        // 4. 验证成功，激活账户
        patient.setStatus(true);
        int updated = patientMapper.updateById(patient);
        
        if (updated > 0) {
            // 5. 删除验证码
            verificationCodeService.deleteCode(request.getIdentificationId());
            log.info("患者账户激活成功，患者ID: {}", patient.getPatientId());
            return Result.success("账户激活成功！");
        } else {
            return Result.error("账户激活失败，请稍后重试");
        }
    }

    /**
     * 检查账户激活状态
     */
    @Transactional(readOnly = true)
    public Result<Boolean> checkActivationStatus(String identificationId) {
        Patient patient = patientMapper.findByIdentificationId(identificationId);
        if (patient == null) {
            return Result.error("未找到该身份证号对应的患者信息");
        }
        boolean isActivated = patient.getStatus() != null && patient.getStatus();
        return Result.success(isActivated);
    }
}
