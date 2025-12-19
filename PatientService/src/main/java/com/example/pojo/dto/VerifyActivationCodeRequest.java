package com.example.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 验证激活码请求
 */
@Data
public class VerifyActivationCodeRequest {
    
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[0-9]{18}$", message = "身份证号必须为18位数字")
    private String identificationId;
    
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^[0-9]{6}$", message = "验证码必须为6位数字")
    private String verificationCode;
}
