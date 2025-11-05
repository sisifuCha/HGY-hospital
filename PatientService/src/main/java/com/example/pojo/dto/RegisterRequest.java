package com.example.pojo.dto;

import com.example.pojo.entity.User;
import lombok.Data;

/**
 * 注册请求的数据传输对象
 */
@Data
public class RegisterRequest extends User {
    // 可以在此添加额外的注册字段，例如确认密码等
    // 目前直接继承 User 实体，包含了所有必要信息
}

