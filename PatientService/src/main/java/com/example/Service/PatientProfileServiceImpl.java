package com.example.Service;

import com.example.Mapper.PatientMapper;
import com.example.pojo.dto.PatientProfileDto;
import com.example.pojo.vo.PatientProfileVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientProfileServiceImpl implements PatientProfileService {

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public void uploadProfile(String patientId, PatientProfileDto profile) {
        // You might want to add validation here to ensure the patient exists
        patientMapper.updateProfile(patientId, profile);
    }

    @Override
    public PatientProfileVo getProfile(String patientId) {
        return patientMapper.findProfileById(patientId);
    }
}

