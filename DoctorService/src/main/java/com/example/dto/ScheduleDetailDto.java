package com.example.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 排班记录详细信息，包含时间模板信息
 */
public class ScheduleDetailDto {
    private String scheduleId;
    private LocalDate scheduleDate;
    private String templateId;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timePeriodName;
    private String docId;

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
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

    public String getTimePeriodName() {
        return timePeriodName;
    }

    public void setTimePeriodName(String timePeriodName) {
        this.timePeriodName = timePeriodName;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}
