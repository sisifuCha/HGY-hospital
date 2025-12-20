package com.example.Service;

import com.example.Mapper.PatientMapper;
import com.example.pojo.vo.PatientDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientDetailServiceImpl implements PatientDetailService {

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public PatientDetailVo getPatientDetail(String patientId) {
        return patientMapper.findPatientDetailById(patientId);
    }
}

