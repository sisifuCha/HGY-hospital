package com.example.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private LocalDateTime payTime;
    private Integer payStatus;  // 0:未支付, 1:已支付, 2:超时, 3:已取消
    private BigDecimal oriAmount;
    private BigDecimal askPayAmount;
    private String patientId;
    private String docId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public BigDecimal getOriAmount() {
        return oriAmount;
    }

    public void setOriAmount(BigDecimal oriAmount) {
        this.oriAmount = oriAmount;
    }

    public BigDecimal getAskPayAmount() {
        return askPayAmount;
    }

    public void setAskPayAmount(BigDecimal askPayAmount) {
        this.askPayAmount = askPayAmount;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}
