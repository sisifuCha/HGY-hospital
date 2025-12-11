package com.example.Service;

import com.example.Mapper.DepartmentMapper;
import com.example.Mapper.RegistrationMapper;
import com.example.conmon.exception.CreateFailedException;
import com.example.conmon.exception.DuplicateRegistrationException;
import com.example.pojo.dto.DepartmentWithSubDepartmentsDto;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.dto.RegistrationDto;
import com.example.pojo.dto.RegistrationQueryDto;
import com.example.pojo.dto.WaitingDto;
import com.example.pojo.entity.Doctor;
import com.example.pojo.vo.PageVo;
import com.example.pojo.vo.RegistrationVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private static final String WAITING_QUEUE_PREFIX = "waiting:queue:";
    private static final String WAITING_SET_PREFIX = "waiting:set:";
    private static final String WAITING_PATIENT_SCHEDULES_PREFIX = "waiting:patient_schedules:";

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
    public RegistrationDto createRegistration(String patientId, String scheduleRecordId) {
        RegistrationDto dto = new RegistrationDto();
        dto.setPatientId(patientId);
        dto.setScheduleRecordId(scheduleRecordId);

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
        
        if (leftSource <= 0) {
            dto.setStatus(false);
            return dto;
        }
        
        // 有号源：扣减号源
        int updated = registrationMapper.decrementScheduleLeftSource(scheduleRecordId);
        if (updated == 0) {
            dto.setStatus(false);
            return dto;
        }
        
        // 插入挂号记录
        int inserted = registrationMapper.insertRegistration(patientId, scheduleRecordId, "待支付");
        if (inserted == 0) {
            // 插入失败，回滚号源扣减
            registrationMapper.incrementScheduleLeftSource(scheduleRecordId);
            throw new CreateFailedException();
        }
        
        // 构造返回对象
        dto.setStatus(true);
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
            // 只有“已挂号”或“待支付”的状态才需要回补号源
            if ("已挂号".equals(status) || "待支付".equals(status)) {
                // 检查候补队列
                String queueKey = WAITING_QUEUE_PREFIX + scheduleRecordId;
                Object nextPatientObj = redisTemplate.opsForList().leftPop(queueKey);

                if (nextPatientObj != null) {
                    // 有候补患者，直接转正
                    WaitingDto waitingDto = (WaitingDto) nextPatientObj;
                    String nextPatientId = waitingDto.getPatientId();
                    log.info("Promoting waiting patient {} for schedule {}", nextPatientId, scheduleRecordId);

                    // 插入挂号记录 (待支付)
                    int inserted = registrationMapper.insertRegistration(nextPatientId, scheduleRecordId, "待支付");
                    if (inserted > 0) {
                        // 清理 Redis 集合
                        String setKey = WAITING_SET_PREFIX + scheduleRecordId;
                        String patientSchedulesKey = WAITING_PATIENT_SCHEDULES_PREFIX + nextPatientId;
                        redisTemplate.opsForSet().remove(setKey, nextPatientId);
                        redisTemplate.opsForSet().remove(patientSchedulesKey, scheduleRecordId);
                        // 号源不回补，因为直接给了候补者
                    } else {
                        log.error("Failed to promote waiting patient {}, releasing source", nextPatientId);
                        registrationMapper.incrementScheduleLeftSource(scheduleRecordId);
                    }
                } else {
                    // 无候补，回补号源
                    registrationMapper.incrementScheduleLeftSource(scheduleRecordId);
                }
            }
        }
        return registrationMapper.findRegistrationByKey(patientId, scheduleRecordId);
    }

    @Override
    public RegistrationVo getRegistrationByPatientAndSchedule(String patientId, String scheduleRecordId) {
        return registrationMapper.findRegistrationByPatientAndSchedule(patientId, scheduleRecordId);
    }
}
