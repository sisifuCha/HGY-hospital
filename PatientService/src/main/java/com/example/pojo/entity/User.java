package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户实体类，对应数据库的 "user" 表
 * 包含所有用户类型（管理员、医生、患者）的通用信息
 */
@Data
@TableName("`user`") // 指定此类映射到数据库中的 user 表
public class User {
    // 用户公共属性
    @TableId("id")
    private String userId;

    // 用户姓名
    @TableField("name")
    private String userName;

    // 性别
    @TableField("sex")
    private String userGender;

    // 登录账号
    @TableField("account")
    private String userAccount;

    // 邮箱
    @TableField("email")
    private String userEmail;

    // 登录密码
    @TableField("pass")
    private String userPassword;

    // 电话号码
    @TableField("phone_num")
    private String userPhone;

    // 用户类型 (ADM:管理员, DOC:医生, PAT:患者)
    @TableField("user_type")
    private String userType;
}