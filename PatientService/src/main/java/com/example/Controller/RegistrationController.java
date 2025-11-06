package com.example.Controller;

import com.example.Service.RegistrationService;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.entity.Doctor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patient/register")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * 根据科室ID和日期获取医生及其排班信息
     * @param departmentId 科室ID
     * @param date 日期
     * @return
     */
    @GetMapping("/doctors/schedules")
    public List<DoctorWithSchedulesDto> getDoctorsWithSchedules(
            @RequestParam String departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return registrationService.getDoctorsWithSchedulesByDepartment(departmentId, date);
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
}

