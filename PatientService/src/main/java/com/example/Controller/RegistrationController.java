package com.example.Controller;

import com.example.Service.RegistrationService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.CreateRegistrationRequest;
import com.example.pojo.dto.DepartmentWithSubDepartmentsDto;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.dto.RegistrationDto;
import com.example.pojo.entity.Doctor;
import jakarta.validation.Valid;
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

    /**
     * 获取所有科室及其子科室
     * @return
     */
    @GetMapping("/departments")
    public List<DepartmentWithSubDepartmentsDto> getDepartments() {
        return registrationService.getAllDepartments();
    }

    /**
     * 创建挂号信息
     * @param req 挂号请求体
     * @return
     */
    @PostMapping("/registrations")
    public Result<RegistrationDto> createRegistration(@RequestBody @Valid CreateRegistrationRequest req) {
        RegistrationDto dto = registrationService.createRegistration(req.getPatientId(), req.getScheduleRecordId(), req.isConfirm());
        return Result.success(dto);
    }

    /**
     * 根据患者ID和排班记录ID获取挂号信息
     * @param patientId 患者ID
     * @param scheduleRecordId 排班记录ID
     * @return
     */
    @GetMapping("/registrations/by-key")
    public Result<RegistrationDto> getRegistrationByKey(@RequestParam String patientId,
                                                        @RequestParam String scheduleRecordId) {
        return Result.success(registrationService.getRegistrationByKey(patientId, scheduleRecordId));
    }

    /**
     * 取消挂号
     * @param patientId 患者ID
     * @param scheduleRecordId 排班记录ID
     * @return
     */
    @DeleteMapping("/registrations")
    public Result<RegistrationDto> cancelRegistration(@RequestParam String patientId,
                                                       @RequestParam String scheduleRecordId) {
        return Result.success(registrationService.cancelRegistration(patientId, scheduleRecordId));
    }
}
