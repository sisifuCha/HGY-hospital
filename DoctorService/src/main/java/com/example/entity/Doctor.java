package com.example.entity;

import java.io.Serializable;

public class Doctor implements Serializable {
    // 继承自user表的基本信息
    private String id;          // 对应user表的ID
    private String email;       // 对应user表的email
    private String pass;        // 对应user表的pass
    private String name;        // 对应user表的name
    private String account;     // 对应user表的account
    private String sex;         // 对应user表的sex
    private String phoneNum;    // 对应user表的phone_num
    
    // doctor表特有信息
    private String docTitleId;  // 对应doctor表的doc_title_ID
    private String status;      // 对应doctor表的status
    private String clinicId;    // 对应doctor表的clinic_ID
    
    // 扩展信息（关联查询）
    private String departmentName;  // 关联department表的name
    private String clinicNumber;    // 关联clinic表的clinic_number
    private String clinicLocation;  // 关联clinic表的location
    private Integer numberSourceCount; // 关联title_number_source表的number_source_count
    private Double oriCost;         // 关联title_number_source表的ori_cost

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getDocTitleId() {
        return docTitleId;
    }

    public void setDocTitleId(String docTitleId) {
        this.docTitleId = docTitleId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getClinicNumber() {
        return clinicNumber;
    }

    public void setClinicNumber(String clinicNumber) {
        this.clinicNumber = clinicNumber;
    }

    public String getClinicLocation() {
        return clinicLocation;
    }

    public void setClinicLocation(String clinicLocation) {
        this.clinicLocation = clinicLocation;
    }

    public Integer getNumberSourceCount() {
        return numberSourceCount;
    }

    public void setNumberSourceCount(Integer numberSourceCount) {
        this.numberSourceCount = numberSourceCount;
    }

    public Double getOriCost() {
        return oriCost;
    }

    public void setOriCost(Double oriCost) {
        this.oriCost = oriCost;
    }
}