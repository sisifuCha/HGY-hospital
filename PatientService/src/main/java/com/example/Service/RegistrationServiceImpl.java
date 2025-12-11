package com.example.Service;

import com.example.Mapper.DepartmentMapper;
import com.example.Mapper.RegistrationMapper;
import com.example.conmon.exception.CreateFailedException;
import com.example.conmon.exception.DuplicateRegistrationException;
import com.example.conmon.exception.SourceFullException;
import com.example.pojo.dto.DepartmentWithSubDepartmentsDto;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.dto.RegistrationDto;
import com.example.pojo.dto.RegistrationQueryDto;
import com.example.pojo.entity.Doctor;
import com.example.pojo.vo.PageVo;
import com.example.pojo.vo.RegistrationVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
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
        // 检查是否已有挂号记录
        Integer dup = registrationMapper.countActiveRegistrationByKey(patientId, scheduleRecordId);
        if (dup != null && dup > 0) {
            throw new DuplicateRegistrationException();
        }
        
        // 检查剩余号源
        Integer leftSource = registrationMapper.findScheduleLeftSource(scheduleRecordId);
        if (leftSource == null) {
            throw new IllegalArgumentException("排班记录不存在");
        }
        
        String status;
        boolean shouldDecrementSource = false;
        
        // 逻辑：有号源时直接预约，无号源时根据confirm参数决定
        if (leftSource > 0) {
            // 有号源：扣减号源，状态为"已预约"
            status = "已预约";
            shouldDecrementSource = true;
        } else {
            // 无号源：根据confirm参数
            if (confirm) {
                // confirm=true但无号源，抛出异常
                throw new SourceFullException();
            } else {
                // confirm=false，无号源
                // 不按API文档创建"预约中"记录，而是直接返回提示，不进行数据库操作
                RegistrationDto dto = new RegistrationDto();
                dto.setPatientId(patientId);
                dto.setScheduleRecordId(scheduleRecordId);
                dto.setStatus("无号源"); // 反馈无号源状态
                return dto;
            }
        }
        
        // 扣减号源（如果需要）
        if (shouldDecrementSource) {
            int updated = registrationMapper.decrementScheduleLeftSource(scheduleRecordId);
            if (updated == 0) {
                throw new SourceFullException();
            }
        }
        
        // 插入挂号记录
        int inserted = registrationMapper.insertRegistration(patientId, scheduleRecordId, status);
        if (inserted == 0) {
            // 插入失败，回滚号源扣减
            if (shouldDecrementSource) {
                registrationMapper.incrementScheduleLeftSource(scheduleRecordId);
            }
            throw new CreateFailedException();
        }
        
        // 构造返回对象
        RegistrationDto dto = new RegistrationDto();
        dto.setPatientId(patientId);
        dto.setScheduleRecordId(scheduleRecordId);
        dto.setStatus(status);
        dto.setRegisterTime(java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        return dto;
    }

    @Override
    public PageVo<RegistrationVo> getRegistrations(RegistrationQueryDto queryDto) {
        if (queryDto.getStatus() != null && !queryDto.getStatus().isEmpty()) {
            queryDto.setStatusList(Arrays.asList(queryDto.getStatus().split(",")));
        }
        int offset = (queryDto.getPage() - 1) * queryDto.getPageSize();
        List<RegistrationVo> registrations = registrationMapper.findRegistrationsByQuery(queryDto, offset, queryDto.getPageSize());
        long total = registrationMapper.countRegistrationsByQuery(queryDto);
        return new PageVo<>(queryDto.getPage(), queryDto.getPageSize(), total, registrations);
    }

    @Override
    public RegistrationDto getRegistrationByKey(String patientId, String scheduleRecordId) {
        return registrationMapper.findRegistrationByKey(patientId, scheduleRecordId);
    }

    @Override
    @Transactional
    public RegistrationDto cancelRegistration(String patientId, String scheduleRecordId) {
        String status = registrationMapper.getRegistrationStatusByKey(patientId, scheduleRecordId);
        if (status == null || "已取消".equals(status) || "已就诊".equals(status)) {
            // 挂号不存在，或已是终态，无法取消
            return null;
        }
        int updated = registrationMapper.updateRegistrationStatusToCanceled(patientId, scheduleRecordId);
        if (updated > 0) {
            // 只有“已预约”的状态才需要回补号源
            if ("已预约".equals(status)) {
                registrationMapper.incrementScheduleLeftSource(scheduleRecordId);
            }
        }
        return registrationMapper.findRegistrationByKey(patientId, scheduleRecordId);
    }

    @Override
    public RegistrationVo getRegistrationByPatientAndSchedule(String patientId, String scheduleRecordId) {
        return registrationMapper.findRegistrationByPatientAndSchedule(patientId, scheduleRecordId);
    }
}
