package com.example.pojo.dto;
public class LoginRequestDTO {
    private String password;

    // 必须有无参构造函数
    public LoginRequestDTO() {}

    // 必须有getter和setter方法
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}