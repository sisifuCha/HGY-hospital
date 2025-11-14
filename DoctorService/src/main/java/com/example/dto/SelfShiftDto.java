package com.example.dto;

import java.time.LocalDate;

public class SelfShiftDto {
    private LocalDate date;
    private Integer timePeriod;
    private String clinicPlace; // 诊室位置（clinic_number）

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(Integer timePeriod) {
        this.timePeriod = timePeriod;
    }

    public String getClinicPlace() {
        return clinicPlace;
    }

    public void setClinicPlace(String clinicPlace) {
        this.clinicPlace = clinicPlace;
    }
}
