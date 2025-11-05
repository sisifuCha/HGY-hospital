package com.example.mapper.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AddNumberApplicationRow {
    private String patientId;
    private String scheduleId;
    private LocalDateTime applyTime;
    private String applicationNote;
    private String patientName;
    private LocalDate scheduleDate;
    private String timePeriodName;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public String getApplicationNote() {
        return applicationNote;
    }

    public void setApplicationNote(String applicationNote) {
        this.applicationNote = applicationNote;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getTimePeriodName() {
        return timePeriodName;
    }

    public void setTimePeriodName(String timePeriodName) {
        this.timePeriodName = timePeriodName;
    }
}
