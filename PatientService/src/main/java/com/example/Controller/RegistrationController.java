package com.example.Controller;

import com.example.Service.RegistrationService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.CreateRegistrationRequest;
import com.example.pojo.dto.DepartmentWithSubDepartmentsDto;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.dto.RegistrationDto;
import com.example.pojo.dto.RegistrationQueryDto;
import com.example.pojo.entity.Doctor;
import com.example.pojo.vo.RegistrationVo;
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
     * @return 医生及其排班信息
     */
    @GetMapping("/registration/doctors")
    public Result<?> getDoctorsWithSchedules(
            @RequestParam String departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            return Result.success(registrationService.getDoctorsWithSchedulesByDepartment(departmentId, date));
        } catch (IllegalArgumentException ex) {
            return Result.fail(404, "科室ID不存在");
        } catch (Exception ex) {
            return Result.fail(400, "日期格式错误");
        }
    }

    /**
     * 根据医生ID获取医生详细信息
     * @param doctorId 医生ID
     * @return 医生详细信息
     */
    @GetMapping("/doctors/{doctorId}")
    public Result<?> getDoctorDetails(@PathVariable String doctorId) {
        return Result.success(registrationService.getDoctorDetailsById(doctorId));
    }

    /**
     * 获取所有科室及其子科室
     * @return 科室及其子科室列表
     */
    @GetMapping("/departments")
    public Result<?> getDepartments() {
        return Result.success(registrationService.getAllDepartments());
    }

    @GetMapping("/registrations")
    public Result<?> getRegistrations(RegistrationQueryDto queryDto) {
        return Result.success(registrationService.getRegistrations(queryDto));
    }

    /**
     * 创建挂号信息
     * @param req 挂号请求体
     * @return 创建的挂号信息
     */
    @PostMapping("/registrations")
    public Result<?> createRegistration(@RequestBody @Valid CreateRegistrationRequest req) {
        RegistrationDto dto = registrationService.createRegistration(req.getPatientId(), req.getScheduleRecordId(), req.isConfirm());
        return Result.success(dto);
    }

    /**
     * 根据患者ID和排班记录ID获取挂号信息
     * @param patientId 患者ID
     * @param scheduleRecordId 排班记录ID
     * @return 挂号信息
     */
    @GetMapping("/registrations/by-key")
    public Result<?> getRegistrationByKey(@RequestParam String patientId,
                                                        @RequestParam String scheduleRecordId) {
        return Result.success(registrationService.getRegistrationByKey(patientId, scheduleRecordId));
    }

    /**
     * 取消挂号
     * @param patientId 患者ID
     * @param scheduleRecordId 排班记录ID
     * @return 取消后的挂号信息
     */
    @DeleteMapping("/registrations")
    public Result<RegistrationDto> cancelRegistration(@RequestParam String patientId,
                                                       @RequestParam String scheduleRecordId) {
        return Result.success(registrationService.cancelRegistration(patientId, scheduleRecordId));
    }
}
