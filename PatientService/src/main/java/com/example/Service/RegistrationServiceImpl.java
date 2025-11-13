package com.example.Service;

import com.example.Mapper.RegistrationMapper;
import com.example.pojo.dto.DepartmentWithSubDepartmentsDto;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.entity.Doctor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    @Override
    public List<DepartmentWithSubDepartmentsDto> getAllDepartments() {

        List<DepartmentWithSubDepartmentsDto> departments = registrationMapper.findAllDepartmentsWithSubDepartments();
        // 使用Jackson库将列表转换为格式化的JSON字符串以便于阅读
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(departments);
            System.out.println("--- Department Query Result ---");
            System.out.println(jsonOutput);
            System.out.println("-----------------------------");
        } catch (JsonProcessingException e) {
            System.err.println("Error converting departments to JSON: " + e.getMessage());
        }
        return departments;
    }
}
