package com.example.Service;

import com.example.pojo.dto.PatientProfileDto;
import com.example.pojo.vo.PatientProfileVo;

public interface PatientProfileService {
    void uploadProfile(String patientId, PatientProfileDto profile);
    PatientProfileVo getProfile(String patientId);
}

