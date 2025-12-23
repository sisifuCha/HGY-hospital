package com.example.Service;

import com.example.Mapper.DepartmentMapper;
import com.example.Mapper.PaymentMapper;
import com.example.Mapper.RegistrationMapper;
import com.example.Mapper.WaitingMapper;
import com.example.conmon.exception.CreateFailedException;
import com.example.conmon.exception.DuplicateRegistrationException;
import com.example.pojo.dto.DepartmentWithSubDepartmentsDto;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.dto.FeePreviewDto;
import com.example.pojo.dto.PaymentDto;
import com.example.pojo.dto.RegistrationDto;
import com.example.pojo.dto.RegistrationQueryDto;
import com.example.pojo.dto.ScheduleDto;
import com.example.pojo.entity.Doctor;
import com.example.pojo.entity.MedicalInsurance;
import com.example.pojo.entity.ReimburseType;
import com.example.pojo.entity.WaitingRecord;
import com.example.pojo.vo.PageVo;
import com.example.pojo.vo.RegistrationVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private PaymentMapper paymentMapper;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private WaitingMapper waitingMapper;
    
    @Autowired
    private MessageService messageService;

    @Override
    public List<DoctorWithSchedulesDto> getDoctorsWithSchedulesByDepartment(String departmentId, LocalDate date) {
        List<DoctorWithSchedulesDto> doctors = registrationMapper.findDoctorsWithSchedulesByDepartmentAndDate(departmentId, date);
        
        // 获取候补规则
        Integer maxWaitingCount = waitingMapper.getRuleValue("MAX_WAITING_COUNT");
        Integer stopHoursBefore = waitingMapper.getRuleValue("STOP_HOURS_BEFORE");
        if (maxWaitingCount == null) maxWaitingCount = 100;
        if (stopHoursBefore == null) stopHoursBefore = 3;
        
        // 为每个排班添加候补信息
        for (DoctorWithSchedulesDto doctor : doctors) {
            if (doctor.getSchedules() != null) {
                for (ScheduleDto schedule : doctor.getSchedules()) {
                    enrichScheduleWithWaitingInfo(schedule, maxWaitingCount, stopHoursBefore);
                }
            }
        }
        
        return doctors;
    }
    
    /**
     * 为排班信息补充候补状态
     */
    private void enrichScheduleWithWaitingInfo(ScheduleDto schedule, Integer maxWaitingCount, Integer stopHoursBefore) {
        // 1. 判断是否可直接挂号
        schedule.setCanRegister(schedule.getLeftSourceCount() != null && schedule.getLeftSourceCount() > 0);
        
        // 2. 获取当前候补人数
        Integer currentWaitingCount = waitingMapper.getWaitingCountBySchedule(schedule.getScheduleId());
        schedule.setWaitingCount(currentWaitingCount != null ? currentWaitingCount : 0);
        
        // 3. 判断候补是否已截止
        try {
            LocalDateTime scheduleStartTime = waitingMapper.getScheduleStartTime(schedule.getScheduleId());
            LocalDateTime stopWaitingTime = scheduleStartTime.minusHours(stopHoursBefore);
            schedule.setWaitingClosed(LocalDateTime.now().isAfter(stopWaitingTime));
        } catch (Exception e) {
            log.warn("Failed to get schedule start time for {}: {}", schedule.getScheduleId(), e.getMessage());
            schedule.setWaitingClosed(false);
        }
        
        // 4. 判断是否可候补：号源为0 且 候补未满 且 未截止
        schedule.setCanWaiting(!schedule.getCanRegister() 
                && schedule.getWaitingCount() < maxWaitingCount 
                && !schedule.getWaitingClosed());
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
            // 插入失败,回滚号源扣减
            registrationMapper.incrementScheduleLeftSource(scheduleRecordId);
            throw new CreateFailedException();
        }
        
        // 挂号成功后自动创建支付订单
        PaymentDto paymentDto = paymentService.createPayment(patientId, scheduleRecordId);
        
        // 发送挂号成功消息
        try {
            String doctorName = paymentDto.getDoctorName() != null ? paymentDto.getDoctorName() : "医生";
            String scheduleTime = dto.getRegisterTime();
            String amount = paymentDto.getAskPayAmount() != null ? paymentDto.getAskPayAmount().toString() : "0";
            messageService.sendRegistrationSuccessMessage(patientId, doctorName, scheduleTime, amount);
        } catch (Exception e) {
            log.warn("Failed to send registration success message: {}", e.getMessage());
        }
        
        // 构造返回对象
        dto.setStatus(true);
        dto.setPaymentId(paymentDto.getPaymentId());
        dto.setAmount(paymentDto.getAskPayAmount());
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
            // 只有"已挂号"或"待支付"的状态才需要回补号源
            if ("已挂号".equals(status) || "待支付".equals(status)) {
                // 从数据库查询下一个候补患者
                WaitingRecord nextWaiting = waitingMapper.getNextWaitingPatient(scheduleRecordId);

                if (nextWaiting != null) {
                    // 有候补患者，直接转正
                    String nextPatientId = nextWaiting.getPatientId();
                    log.info("Promoting waiting patient {} for schedule {}", nextPatientId, scheduleRecordId);

                    // 插入挂号记录 (待支付)
                    int inserted = registrationMapper.insertRegistration(nextPatientId, scheduleRecordId, "待支付");
                    if (inserted > 0) {
                        // 候补转正成功后，自动创建订单
                        try {
                            PaymentDto promotedPayment = paymentService.createPayment(nextPatientId, scheduleRecordId);
                            log.info("Payment order created for promoted patient {}", nextPatientId);
                            
                            // 更新候补记录状态为已转正
                            waitingMapper.promoteWaitingRecord(nextPatientId, scheduleRecordId, LocalDateTime.now());
                            
                            // 发送候补转正通知
                            String doctorName = promotedPayment.getDoctorName() != null ? promotedPayment.getDoctorName() : "医生";
                            String timePeriod = "就诊时段"; // 可以从排班信息获取
                            messageService.sendWaitingPromotedMessage(nextPatientId, scheduleRecordId, doctorName, timePeriod);
                        } catch (Exception e) {
                            log.error("Failed to create payment for promoted patient {}: {}", nextPatientId, e.getMessage());
                        }
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

    @Override
    public FeePreviewDto getFeePreview(String patientId, String scheduleRecordId) {
        // 1. 获取排班基本信息（医生、科室、日期、时段、挂号费）
        FeePreviewDto dto = registrationMapper.findScheduleInfoForFeePreview(scheduleRecordId);
        if (dto == null) {
            throw new IllegalArgumentException("排班记录不存在");
        }

        BigDecimal registrationFee = dto.getRegistrationFee();
        if (registrationFee == null) {
            registrationFee = BigDecimal.ZERO;
        }

        // 2. 获取患者的报销类型和比例
        ReimburseType reimburseType = paymentMapper.getReimburseTypeByPatient(patientId);
        if (reimburseType != null && reimburseType.getType() != null) {
            dto.setReimburseType(reimburseType.getType());
            BigDecimal percent = reimburseType.getPercent();
            if (percent != null) {
                dto.setReimbursePercent(percent);
                // 计算报销金额 = 挂号费 × 报销比例 / 100
                BigDecimal reimbursedAmount = registrationFee.multiply(percent)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                dto.setReimbursedAmount(reimbursedAmount);
                // 实际支付 = 挂号费 - 报销金额
                dto.setActualPayAmount(registrationFee.subtract(reimbursedAmount));
            } else {
                dto.setReimbursePercent(BigDecimal.ZERO);
                dto.setReimbursedAmount(BigDecimal.ZERO);
                dto.setActualPayAmount(registrationFee);
            }
        } else {
            // 无医保
            dto.setReimburseType("无医保");
            dto.setReimbursePercent(BigDecimal.ZERO);
            dto.setReimbursedAmount(BigDecimal.ZERO);
            dto.setActualPayAmount(registrationFee);
        }

        // 3. 获取患者的医保账户余额
        String medicalInsuranceId = paymentMapper.findMedicalInsuranceIdByPatient(patientId);
        if (medicalInsuranceId != null) {
            MedicalInsurance insurance = paymentMapper.getMedicalInsurance(medicalInsuranceId);
            if (insurance != null && insurance.getOverage() != null) {
                dto.setMedicalInsuranceBalance(insurance.getOverage());
                // 判断余额是否足够
                dto.setCanAfford(insurance.getOverage().compareTo(dto.getActualPayAmount()) >= 0);
            } else {
                dto.setMedicalInsuranceBalance(BigDecimal.ZERO);
                dto.setCanAfford(false);
            }
        } else {
            dto.setMedicalInsuranceBalance(BigDecimal.ZERO);
            dto.setCanAfford(false);
        }

        return dto;
    }
}
