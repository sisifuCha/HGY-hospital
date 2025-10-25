//package com.example.pojo.entity;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@TableName("doctor") // 如果使用MyBatis-Plus
//public class Doctor {
//    @TableId(value = "id", type = IdType.INPUT)
//    private String id;
//
//    @TableField("account_name")
//    private String accountName;
//
//    private String name;
//    private String gender;
//    private String password;
//    private String title;
//    private String department;
//    private String email;
//    private String phone;
//
//    @TableField("create_time")
//    private LocalDateTime createTime;
//
//    @TableField("update_time")
//    private LocalDateTime updateTime;
//}