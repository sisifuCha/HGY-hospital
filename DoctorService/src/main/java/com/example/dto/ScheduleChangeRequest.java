package com.example.dto;

public class ScheduleChangeRequest {
    private String docID;
    private String originalScheduleID;
    private Integer changeType; // 0:调班, 1:请假
    private String targetDate;
    private Integer targetTimeSlot;
    private String targetDoctorID;
    private Integer leaveTimeLength;
    private String reason;

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getOriginalScheduleID() {
        return originalScheduleID;
    }

    public void setOriginalScheduleID(String originalScheduleID) {
        this.originalScheduleID = originalScheduleID;
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

    public Integer getTargetTimeSlot() {
        return targetTimeSlot;
    }

    public void setTargetTimeSlot(Integer targetTimeSlot) {
        this.targetTimeSlot = targetTimeSlot;
    }

    public String getTargetDoctorID() {
        return targetDoctorID;
    }

    public void setTargetDoctorID(String targetDoctorID) {
        this.targetDoctorID = targetDoctorID;
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
