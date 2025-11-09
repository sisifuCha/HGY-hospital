package com.example.dto;

public class ScheduleChangeRequest {
    private String docId;
    private String originalTime; // 格式: ${date}_${timePeriod}, 例如: 2025-11-11_2
    private Integer changeType; // 0:调班, 1:请假
    private String targetDate;
    private Integer timePeriod;
    private String targetDoctorId;
    private Integer leaveTimeLength;
    private String reason;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getOriginalTime() {
        return originalTime;
    }

    public void setOriginalTime(String originalTime) {
        this.originalTime = originalTime;
    }

    public Integer getChangeType() {
        return changeType;
    }

    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public Integer getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(Integer timePeriod) {
        this.timePeriod = timePeriod;
    }

    public String getTargetDoctorId() {
        return targetDoctorId;
    }

    public void setTargetDoctorId(String targetDoctorId) {
        this.targetDoctorId = targetDoctorId;
    }

    public Integer getLeaveTimeLength() {
        return leaveTimeLength;
    }

    public void setLeaveTimeLength(Integer leaveTimeLength) {
        this.leaveTimeLength = leaveTimeLength;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
