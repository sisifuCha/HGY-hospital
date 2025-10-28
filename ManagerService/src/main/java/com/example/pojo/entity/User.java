package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("`user`") // 指定此类映射到数据库中的 user 表
public class User {
    // 用户公共属性
    @TableId // 主键
    @TableField("id")
    private String userId;
    @TableField("name")
    private String userName;
    @TableField("sex")
    private String userGender;
    @TableField("account")
    private String userAccount;
    @TableField("email")
    private String userEmail;
    @TableField("pass")
    private String userPassword;
    @TableField("phone_num")
    private String userPhone;
    @TableField("user_type")
    private String userType;
}