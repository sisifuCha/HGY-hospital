package com.example.dto;

import java.util.List;

/**
 * 就诊评价请求体，与 patient_message.yaml 保持一致。
 */
public class FeedbackRequest {
    private String patientId;
    private Integer score;
    private List<String> tags;
    private String comment;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

