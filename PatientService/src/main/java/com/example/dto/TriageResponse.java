package com.example.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能导诊响应/历史记录条目
 */
public class TriageResponse {
    private String triageId;
    private String patientId;

    private List<TriageRecommendation> recommendations;
    private List<String> suggestedActions;

    private LocalDateTime createdTime;

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

    public List<TriageRecommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<TriageRecommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public List<String> getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(List<String> suggestedActions) {
        this.suggestedActions = suggestedActions;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}

