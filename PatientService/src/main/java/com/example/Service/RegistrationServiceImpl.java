package com.example.Service;

import com.example.Mapper.RegistrationMapper;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.entity.Doctor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private RegistrationMapper registrationMapper;

    @Override
    public List<DoctorWithSchedulesDto> getDoctorsWithSchedulesByDepartment(String departmentId, LocalDate date) {
        return registrationMapper.findDoctorsWithSchedulesByDepartmentAndDate(departmentId, date);
    }

    @Override
    public Doctor getDoctorDetailsById(String doctorId) {
        return registrationMapper.findDoctorDetailsById(doctorId);
    }
}

