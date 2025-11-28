package com.example.Service;

import com.example.pojo.dto.DepartmentWithSubDepartmentsDto;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.entity.Doctor;

import java.time.LocalDate;
import java.util.List;

public interface RegistrationService {
    List<DoctorWithSchedulesDto> getDoctorsWithSchedulesByDepartment(String departmentId, LocalDate date);
    Doctor getDoctorDetailsById(String doctorId);
    List<DepartmentWithSubDepartmentsDto> getAllDepartments();
}
