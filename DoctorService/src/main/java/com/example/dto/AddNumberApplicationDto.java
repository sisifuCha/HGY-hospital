package com.example.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class AddNumberApplicationDto {
    private String addId;
    private String patientName;
    private OffsetDateTime applyTime;
    private LocalDate targetDate;
    private int targetTimePeriod;
    private String note;

    public String getAddId() {
        return addId;
    }

    public void setAddId(String addId) {
        this.addId = addId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public OffsetDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(OffsetDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public int getTargetTimePeriod() {
        return targetTimePeriod;
    }

    public void setTargetTimePeriod(int targetTimePeriod) {
        this.targetTimePeriod = targetTimePeriod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
