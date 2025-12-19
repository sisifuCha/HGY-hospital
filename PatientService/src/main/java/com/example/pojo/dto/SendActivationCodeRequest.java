package com.example.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送激活验证码请求
 */
@Data
public class SendActivationCodeRequest {
    
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[0-9]{18}$", message = "身份证号必须为18位数字")
    private String identificationId;
    
    @NotBlank(message = "姓名不能为空")
    private String userName;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
