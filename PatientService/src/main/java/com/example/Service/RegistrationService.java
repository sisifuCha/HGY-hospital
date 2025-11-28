package com.example.Service;

import com.example.pojo.dto.DepartmentWithSubDepartmentsDto;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.dto.RegistrationDto;
import com.example.pojo.dto.RegistrationQueryDto;
import com.example.pojo.entity.Doctor;
import com.example.pojo.vo.PageVo;
import com.example.pojo.vo.RegistrationVo;

import java.time.LocalDate;
import java.util.List;

public interface RegistrationService {
    List<DoctorWithSchedulesDto> getDoctorsWithSchedulesByDepartment(String departmentId, LocalDate date);
    Doctor getDoctorDetailsById(String doctorId);
    List<DepartmentWithSubDepartmentsDto> getAllDepartments();

    RegistrationDto createRegistration(String patientId, String scheduleRecordId, boolean confirm);
    RegistrationDto cancelRegistration(String patientId, String scheduleRecordId);
    RegistrationDto getRegistrationByKey(String patientId, String scheduleRecordId);

    PageVo<RegistrationVo> getRegistrations(RegistrationQueryDto queryDto);

    RegistrationVo getRegistrationByPatientAndSchedule(String patientId, String scheduleRecordId);
}
