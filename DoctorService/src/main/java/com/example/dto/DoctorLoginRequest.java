package com.example.dto;

public class DoctorLoginRequest {
    // private String docID;
    private String docAccount;//用于登录
    private String pass;

    // public String getDocID() {
    //     return docID;
    // }

    // public void setDocID(String docID) {
    //     this.docID = docID;
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