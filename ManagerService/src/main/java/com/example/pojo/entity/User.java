package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("\"user\"") // PostgreSQL中user是关键字，需要用双引号包裹
public class User {
    // 用户公共属性
    @TableId(value = "id") // 主键，指定数据库列名为id
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