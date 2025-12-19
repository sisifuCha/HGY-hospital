package com.example.dto;

import java.time.LocalDateTime;

/**
 * triage_record 表的行模型（MyBatis 直接映射），
 * 复杂字段用 JSON 字符串承载，Service 层再转换成对外 DTO。
 */
public class TriageRecordRow {
    private String triageId;
    private String patientId;

    /** JSON string */
    private String symptoms;
    /** JSON string */
    private String recommendations;
    /** JSON string */
    private String suggestedActions;

    private String mode;
    private LocalDateTime createdAt;

    public String getTriageId() {
        return triageId;
    }

    public void setTriageId(String triageId) {
        this.triageId = triageId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(String suggestedActions) {
        this.suggestedActions = suggestedActions;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

