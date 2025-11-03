package com.example.mapper.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class DepartmentShiftRow {
    private LocalDate scheduleDate;
    private String doctorName;
    private String doctorId;
    private String timePeriodName;
    private LocalTime startTime;

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getTimePeriodName() {
        return timePeriodName;
    }

    public void setTimePeriodName(String timePeriodName) {
        this.timePeriodName = timePeriodName;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
}
