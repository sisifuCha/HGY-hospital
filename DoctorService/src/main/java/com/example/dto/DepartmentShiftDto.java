package com.example.dto;

import java.time.LocalDate;

public class DepartmentShiftDto {
    private LocalDate date;
    private String docName;
    private int timePeriod;
    private String docId;
    private String clinicPlace; // 诊室位置（clinic_number）

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public int getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(int timePeriod) {
        this.timePeriod = timePeriod;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getClinicPlace() {
        return clinicPlace;
    }

    public void setClinicPlace(String clinicPlace) {
        this.clinicPlace = clinicPlace;
    }
}
