package com.example.entity;

import java.io.Serializable;
import java.time.LocalTime;

public class ScheduleTemplate implements Serializable {
    private String id;
    private LocalTime startTime;
    private LocalTime endTime;
    private String clinId;
    private String timePeriodName;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getClinId() {
        return clinId;
    }

    public void setClinId(String clinId) {
        this.clinId = clinId;
    }

    public String getTimePeriodName() {
        return timePeriodName;
    }

    public void setTimePeriodName(String timePeriodName) {
        this.timePeriodName = timePeriodName;
    }
}
