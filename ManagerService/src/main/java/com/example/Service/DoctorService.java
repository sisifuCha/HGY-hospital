package com.example.Service;

import com.example.conmon.result.Result;
import com.example.pojo.dto.DoctorDTO;
//import com.example.pojo.entity.Doctor;

public interface DoctorService {
    /**
     * 更新医生信息
     */
    Result<String> updateDoctor(DoctorDTO doctorDTO);

    /**
     * 根据ID获取医生信息
     */
    //Result<Doctor> getDoctorById(String id);
}