package com.example.dto;

public class PatientStatusRequest {
    private String doctorId;
    private Integer doctorStatus;  // 0:空闲, 1:坐诊
    private String registerId;
    private Integer patientStatus; // 0:未就诊, 1:就诊中, 2:完成

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public Integer getDoctorStatus() {
        return doctorStatus;
    }

    public void setDoctorStatus(Integer doctorStatus) {
        this.doctorStatus = doctorStatus;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public Integer getPatientStatus() {
        return patientStatus;
    }

    public void setPatientStatus(Integer patientStatus) {
        this.patientStatus = patientStatus;
    }
}