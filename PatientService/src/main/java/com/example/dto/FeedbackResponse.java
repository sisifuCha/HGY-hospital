package com.example.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 就诊评价返回体，与 patient_message.yaml 保持一致。
 */
public class FeedbackResponse {
    private String registrationId;
    private Integer score;
    private List<String> tags;
    private String comment;
    private LocalDateTime createdTime;

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
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

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}

