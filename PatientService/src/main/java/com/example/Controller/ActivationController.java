package com.example.Controller;

import com.example.Service.ActivationService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.SendActivationCodeRequest;
import com.example.pojo.dto.VerifyActivationCodeRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 账户激活控制器
 */
@RestController
@RequestMapping("/user/activation")
public class ActivationController {

    @Autowired
    private ActivationService activationService;

    /**
     * 发送激活验证码
     * 第一步：前端提供身份证号、姓名和邮箱，后端发送验证码
     * 
     * @param request 包含身份证号、姓名、邮箱
     * @return 发送结果
     */
    @PostMapping("/send-code")
    public Result<String> sendActivationCode(@RequestBody @Valid SendActivationCodeRequest request) {
        return activationService.sendActivationCode(request);
    }

    /**
     * 验证激活码并激活账户
     * 第二步：前端提供身份证号和验证码，后端验证并激活账户
     * 
     * @param request 包含身份证号和验证码
     * @return 验证和激活结果
     */
    @PostMapping("/verify")
    public Result<String> verifyActivationCode(@RequestBody @Valid VerifyActivationCodeRequest request) {
        return activationService.verifyAndActivate(request);
    }

    /**
     * 检查账户激活状态
     * 
     * @param identificationId 身份证号
     * @return 激活状态
     */
    @GetMapping("/status")
    public Result<Boolean> checkActivationStatus(@RequestParam String identificationId) {
        return activationService.checkActivationStatus(identificationId);
    }
}
