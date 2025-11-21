package com.example.entity;

import java.io.Serializable;
import java.util.Date;

public class DocScheduleRecord implements Serializable {
    private String id;
    private String templateId;
    private Date scheduleDate;
    private Integer leftSourceCount;
    private String status; // '0' or '1' or other
    private String docId;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Date getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public Integer getLeftSourceCount() {
        return leftSourceCount;
    }

    public void setLeftSourceCount(Integer leftSourceCount) {
        this.leftSourceCount = leftSourceCount;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}