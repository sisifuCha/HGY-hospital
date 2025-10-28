package com.example.pojo.dto;
public class LoginRequest {
    private String password;

    // 必须有无参构造函数
    public LoginRequest() {}

    // 必须有getter和setter方法
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}