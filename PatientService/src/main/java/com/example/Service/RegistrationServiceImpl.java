package com.example.Service;

import com.example.Mapper.DepartmentMapper;
import com.example.Mapper.RegistrationMapper;
import com.example.conmon.exception.CreateFailedException;
import com.example.conmon.exception.DuplicateRegistrationException;
import com.example.conmon.exception.SourceFullException;
import com.example.conmon.result.Result;
import com.example.pojo.dto.DepartmentWithSubDepartmentsDto;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.dto.RegistrationDto;
import com.example.pojo.entity.Doctor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

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
        List<DepartmentWithSubDepartmentsDto> departments = departmentMapper.findAllDepartmentsWithSubDepartments();
        // 调试信息改为可控日志，避免控制台噪音
        if (log.isDebugEnabled()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                log.debug("Department Query Result: {}", objectMapper.writeValueAsString(departments));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize departments for debug: {}", e.getMessage());
            }
        }
        return departments;
    }

    @Override
    @Transactional
    public RegistrationDto createRegistration(String patientId, String scheduleRecordId, boolean confirm) {
        Integer dup = registrationMapper.countActiveRegistrationByKey(patientId, scheduleRecordId);
        if (dup != null && dup > 0) {
            throw new DuplicateRegistrationException();
        }
        // 中文枚举：预约中 / 已预约 / 已就诊 / 已取消 / 已过期
        String status = confirm ? "已预约" : "预约中";
        if (confirm) {
            int updated = registrationMapper.decrementScheduleLeftSource(scheduleRecordId);
            if (updated == 0) {
                throw new SourceFullException();
            }
        }
        int inserted = registrationMapper.insertRegistration(patientId, scheduleRecordId, status);
        if (inserted == 0) {
            if (confirm) {
                registrationMapper.incrementScheduleLeftSource(scheduleRecordId);
            }
            throw new CreateFailedException();
        }
        RegistrationDto dto = new RegistrationDto();
        dto.setPatientId(patientId);
        dto.setScheduleRecordId(scheduleRecordId);
        dto.setStatus(status);
        dto.setRegisterTime(java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        return dto;
    }

    @Override
    public RegistrationDto getRegistrationByKey(String patientId, String scheduleRecordId) {
        return registrationMapper.findRegistrationByKey(patientId, scheduleRecordId);
    }

    @Override
    @Transactional
    public RegistrationDto cancelRegistration(String patientId, String scheduleRecordId) {
        String currentStatus = registrationMapper.getRegistrationStatusByKey(patientId, scheduleRecordId);
        if (currentStatus == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        if (!("预约中".equals(currentStatus) || "已预约".equals(currentStatus))) {
            throw new IllegalArgumentException("当前状态不可取消");
        }
        boolean needRollbackSource = "已预约".equals(currentStatus);
        int updated = registrationMapper.updateRegistrationStatusToCanceled(patientId, scheduleRecordId);
        if (updated == 0) {
            throw new CreateFailedException("取消失败");
        }
        if (needRollbackSource) {
            registrationMapper.incrementScheduleLeftSource(scheduleRecordId);
        }
        RegistrationDto dto = registrationMapper.findRegistrationByKey(patientId, scheduleRecordId);
        return dto;
    }
}
