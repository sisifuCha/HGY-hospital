package com.example.Controller;

import com.example.Service.RegistrationService;
import com.example.pojo.dto.DepartmentWithSubDepartmentsDto;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.entity.Doctor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * 根据科室ID和日期获取医生及其排班信息
     * @param departmentId 科室ID
     * @param date 日期
     * @return
     */
    @GetMapping("/registration/doctors")
    public List<DoctorWithSchedulesDto> getDoctorsWithSchedules(
            @RequestParam Integer departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return registrationService.getDoctorsWithSchedulesByDepartment(String.valueOf(departmentId), date);
    }

    /**
     * 根据医生ID获取医生详细信息
     * @param doctorId 医生ID
     * @return
     */
    @GetMapping("/doctors/{doctorId}")
    public Doctor getDoctorDetails(@PathVariable String doctorId) {
        return registrationService.getDoctorDetailsById(doctorId);
    }

    /**
     * 获取所有科室及其子科室
     * @return
     */
    @GetMapping("/departments")
    public List<DepartmentWithSubDepartmentsDto> getDepartments() {
        return registrationService.getAllDepartments();
    }
}
