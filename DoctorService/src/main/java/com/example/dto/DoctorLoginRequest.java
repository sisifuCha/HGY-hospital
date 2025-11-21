package com.example.dto;

public class DoctorLoginRequest {
    // private String docId;
    private String docAccount;//用于登录
    private String pass;

    // public String getDocId() {
    //     return docId;
    // }

    // public void setDocId(String docId) {
    //     this.docId = docId;
    // }
    public String getDocAccount() {
        return docAccount;
    }

    public void setDocAccount(String docAccount) {
        this.docAccount = docAccount;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}