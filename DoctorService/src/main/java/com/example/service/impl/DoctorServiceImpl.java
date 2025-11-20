package com.example.service.impl;

import com.example.dto.AddNumberApplicationDto;
import com.example.dto.AddNumberDecisionRequest;
import com.example.dto.DepartmentShiftDto;
import com.example.dto.DoctorLoginRequest;
import com.example.dto.DoctorProfileDto;
import com.example.dto.DoctorProfileUpdateRequest;
import com.example.dto.NotificationMessageDto;
import com.example.dto.PatientRecordDto;
import com.example.dto.PatientStatusRequest;
import com.example.dto.PatientSummaryDto;
import com.example.dto.ScheduleChangeRequest;
import com.example.dto.SelfShiftDto;
import com.example.entity.Doctor;
import com.example.entity.DocScheduleRecord;
import com.example.entity.AddNumberSourceRecord;
import com.example.entity.PayRecord;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.example.utils.*;
import com.example.mapper.DoctorMapper;
import com.example.mapper.DocScheduleRecordMapper;
import com.example.mapper.DocScheduleChangeRecordMapper;
import com.example.mapper.RegisterRecordMapper;
import com.example.mapper.AddNumberSourceRecordMapper;
import com.example.mapper.PayRecordMapper;
import com.example.mapper.DocScheduleChangeRecordMapper.ScheduleChangeRecordRow;
import com.example.mapper.model.AddNumberApplicationRow;
import com.example.mapper.model.DepartmentShiftRow;
import com.example.mapper.model.PatientRecordRow;
import com.example.mapper.model.PatientSummaryRow;
import com.example.mapper.model.SelfShiftRow;
import com.example.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorServiceImpl implements DoctorService {
    
    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private DocScheduleRecordMapper scheduleRecordMapper;

    @Autowired
    private RegisterRecordMapper registerRecordMapper;

    @Autowired
    private AddNumberSourceRecordMapper addNumberSourceRecordMapper;

    @Autowired
    private DocScheduleChangeRecordMapper scheduleChangeRecordMapper;

    @Autowired
    private PayRecordMapper payRecordMapper;

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    // 存储SSE连接
    private final ConcurrentHashMap<String, SseEmitter> addNumberEmitters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SseEmitter> notificationEmitters = new ConcurrentHashMap<>();

    @Override
    // Service 层只关注业务结果，可以返回一个封装了状态的对象，或者抛出业务异常。
    public Result<Map<String, String>> login(DoctorLoginRequest request){
        // 1. 检查医生id和密码是否为空 (业务输入验证)
        if (!StringUtils.hasText(request.getDocAccount()) || !StringUtils.hasText(request.getPass())) {
            return Result.fail(400, "医生账号或密码不能为空");
        }
        
        // 2. 查询医生信息 (协调数据访问)
        Doctor doctor = doctorMapper.getDoctorByAccount(request.getDocAccount());
        
        if (doctor == null) {
            // 3. 医生不存在 (业务判断)
            return Result.fail(401, "医生账号或密码错误");
        }
        
        // 4. 验证密码 (核心业务逻辑)
        if (!doctor.getPass().equals(request.getPass())) {
            System.out.println("医生登录失败，医生id: " + doctor.getId() + ", 输入密码错误");
            return Result.fail(401, "医生账号或密码错误");
        }
        
        // 5. 生成JWT令牌 (业务功能实现)
        try {
            String jwtToken = JwtUtil.generateToken(doctor.getId());
            System.out.println("医生登录成功，医生id: " + doctor.getId() + ", 生成的JWT: " + jwtToken);
            return Result.loginSuccess(doctor.getId(),jwtToken);
            
        } catch (Exception e) {
            // JWT生成失败
            return Result.fail(500, "系统错误，令牌生成失败");
        }

}

    @Override
    public SseEmitter getAddNumberNotifications(String docId) {
        SseEmitter emitter = createEmitter(addNumberEmitters, docId);
        emitAddNumberSnapshot(docId, emitter);
        return emitter;
    }

    @Override
    @Transactional
    public Result<java.util.Map<String, String>> reviewAddNumberRequest(AddNumberDecisionRequest request) {
        if (request == null || !StringUtils.hasText(request.getAddId())) {
            return Result.fail(400, "加号id不能为空");
        }

        RegisterIdUtil.RegisterKey key;
        try {
            key = RegisterIdUtil.decode(request.getAddId());
        } catch (IllegalArgumentException ex) {
            return Result.fail(400, "加号id格式不正确");
        }

        AddNumberSourceRecord record = addNumberSourceRecordMapper.getRequest(key.getPatientId(), key.getScheduleId());
        if (record == null) {
            return Result.fail(404, "未找到加号申请");
        }

        DocScheduleRecord schedule = scheduleRecordMapper.getScheduleById(key.getScheduleId());
        if (schedule == null) {
            return Result.fail(404, "未找到关联排班记录");
        }

        String status = request.isApproved() ? "已同意" : "已拒绝";
        int affected = addNumberSourceRecordMapper.updateRequestStatus(key.getPatientId(), key.getScheduleId(), status);
        if (affected == 0) {
            return Result.fail(500, "更新加号申请失败");
        }

        // 如果审核通过,生成支付订单(后台处理,医生端不返回订单信息)
        if (request.isApproved()) {
            try {
                // 查询医生职称ID
                String docTitleId = doctorMapper.getDoctorTitleId(schedule.getDocId());
                if (!StringUtils.hasText(docTitleId)) {
                    return Result.fail(500, "医生信息不完整,无法生成订单");
                }
                
                // 查询职称对应的原价
                BigDecimal oriCost = payRecordMapper.getTitleOriCost(docTitleId);
                if (oriCost == null) {
                    return Result.fail(500, "未找到医生职称费用信息");
                }
                
                // 查询患者报销比例
                Integer reimbursePercent = payRecordMapper.getPatientReimbursePercent(key.getPatientId());
                if (reimbursePercent == null) {
                    reimbursePercent = 0; // 默认不报销
                }
                
                // 计算实际支付金额: ori_amount * (1 - percent/100)
                BigDecimal askPayAmount = oriCost.multiply(
                    BigDecimal.valueOf(100 - reimbursePercent)
                ).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                
                // 先检查 schedule.status 是否为 '0' 或 '1'
                // DocScheduleRecord 实体当前未包含 status 字段，假设 mapper 能返回 status 字符串
                String scheduleStatus = schedule.getStatus();
                if (!"0".equals(scheduleStatus) && !"1".equals(scheduleStatus)) {
                    return Result.fail(400, "该排班不可加号");
                }

                // 插入 register_record (status='待支付')
                com.example.entity.RegisterRecord rr = new com.example.entity.RegisterRecord();
                rr.setPatientId(key.getPatientId());
                rr.setSchId(key.getScheduleId());
                rr.setRegisterTime(new java.util.Date());
                rr.setStatus("待支付");
                int rInserted = registerRecordMapper.insertRegisterRecord(rr);
                if (rInserted == 0) {
                    return Result.fail(500, "插入挂号记录失败");
                }

                // 创建支付记录，使用 UUID 作为 id，pay_status 使用中文 '待支付'
                PayRecord payRecord = new PayRecord();
                payRecord.setId(java.util.UUID.randomUUID().toString());
                payRecord.setPayStatus("待支付");
                payRecord.setOriAmount(oriCost);
                payRecord.setAskPayAmount(askPayAmount);
                payRecord.setPatientId(key.getPatientId());
                payRecord.setSchId(schedule.getId());
                // pay_time为null,待支付时更新

                int inserted = payRecordMapper.insertPayRecord(payRecord);
                if (inserted == 0) {
                    return Result.fail(500, "生成支付订单失败");
                }
                
            } catch (Exception e) {
                return Result.fail(500, "生成支付订单时发生错误: " + e.getMessage());
            }
        }
        
        java.util.Map<String, String> payload = new java.util.HashMap<>();
        payload.put("decision", status);
        payload.put("message", request.isApproved() ? "加号申请已批准" : "加号申请已拒绝");

        // 推送给医生的订阅端以更新申请列表
        emitAddNumberSnapshot(schedule.getDocId(), null);

        return Result.success(payload, "审核完成");
    }

    @Override
    public List<DepartmentShiftDto> getDepartmentShifts(String docId) {
        List<DepartmentShiftRow> rows = scheduleRecordMapper.selectDepartmentShiftRows(docId, LocalDate.now());
        return rows.stream()
                .map(this::mapDepartmentShift)
                .collect(Collectors.toList());
    }

    @Override
    public List<SelfShiftDto> getSelfShifts(String docId) {
        List<SelfShiftRow> rows = scheduleRecordMapper.selectSelfShiftRows(docId, LocalDate.now());
        return rows.stream()
                .map(this::mapSelfShift)
                .collect(Collectors.toList());
    }

    @Override
    public List<PatientSummaryDto> getPatientList(String docId) {
        List<PatientSummaryRow> rows = registerRecordMapper.selectPatientSummaryRows(docId);
        return rows.stream()
                .map(this::mapPatientSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<PatientRecordDto> getPatientDetails(String docId, String registerId) {
        RegisterIdUtil.RegisterKey key;
        try {
            key = RegisterIdUtil.decode(registerId);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("挂号id格式不正确");
        }

        DocScheduleRecord schedule = scheduleRecordMapper.getScheduleById(key.getScheduleId());
        if (schedule == null) {
            throw new IllegalArgumentException("未找到挂号记录");
        }
        if (StringUtils.hasText(docId) && !docId.equals(schedule.getDocId())) {
            throw new IllegalArgumentException("无权访问该患者信息");
        }

        List<PatientRecordRow> rows = registerRecordMapper.selectPatientHistoryRows(key.getPatientId());
        return rows.stream()
                .map(this::mapPatientRecord)
                .collect(Collectors.toList());
    }

    @Override
    public SseEmitter getSystemNotifications(String docId) {
        SseEmitter emitter = createEmitter(notificationEmitters, docId);
        emitNotificationSnapshot(docId, emitter);
        return emitter;
    }

    @Override
    public DoctorProfileDto getDoctorProfile(String docId) {
        Doctor doctor = doctorMapper.getDoctorWithDetails(docId);
        if (doctor == null) {
            throw new IllegalArgumentException("医生不存在");
        }
        DoctorProfileDto dto = new DoctorProfileDto();
        dto.setDoctorId(doctor.getId());
        dto.setName(doctor.getName());
        dto.setDepartment(doctor.getDepartmentName());
        dto.setTitle(StringUtils.hasText(doctor.getTitleName()) ? doctor.getTitleName() : "");
        return dto;
    }

    @Override
    @Transactional
    public Result<Void> submitScheduleChangeRequest(ScheduleChangeRequest request) {
        // 1. 参数校验
        if (request == null || !StringUtils.hasText(request.getDocId()) || !StringUtils.hasText(request.getOriginalTime())) {
            return Result.fail(400, "docId和originalTime为必填参数");
        }
        if (request.getChangeType() == null || request.getChangeType() < 0 || request.getChangeType() > 2) {
            return Result.fail(400, "changeType必须为0/1/2");
        }

        // 2. 解析 originalTime: 格式 "2025-11-22_1"
        String[] parts = request.getOriginalTime().split("_");
        if (parts.length != 2) {
            return Result.fail(400, "originalTime格式不正确，应为: 日期_时段编号");
        }

        LocalDate originalDate;
        Integer originalTimePeriod;
        try {
            originalDate = LocalDate.parse(parts[0]);
            originalTimePeriod = Integer.parseInt(parts[1]);
        } catch (Exception ex) {
            return Result.fail(400, "originalTime格式不正确: " + ex.getMessage());
        }

        // 3. 查询原班次记录（ori_sch_id）
        String originalTemplateId = mapTimePeriodIndexToTemplateId(originalTimePeriod);
        if (originalTemplateId == null) {
            return Result.fail(400, "时段编号不正确，应为1/2/3");
        }

        DocScheduleRecord originalSchedule = scheduleRecordMapper.findByDocAndDateAndPeriod(
            request.getDocId(), 
            originalDate, 
            originalTemplateId
        );
        if (originalSchedule == null) {
            return Result.fail(404, "未找到原班次记录");
        }

        // 4. 检查是否存在待处理的重复申请
        int pendingCount = scheduleChangeRecordMapper.countPendingByDocAndSchedule(
            request.getDocId(),
            originalSchedule.getId()
        );
        if (pendingCount > 0) {
            return Result.fail(409, "该班次已有待处理的变更申请，请勿重复提交");
        }

        // 5. 根据 changeType 处理不同逻辑
        String targetSchId = null;
        LocalDate targetDate = null;
        String templateId = null;
        Integer leaveTimeLength = null;

        if (request.getChangeType() == 0) {
            // 调到空班：需要 targetDate 和 timePeriod
            if (request.getTargetDate() == null || request.getTimePeriod() == null) {
                return Result.fail(400, "调到空班需提供targetDate和timePeriod");
            }
            
            targetDate = request.getTargetDate();
            templateId = mapTimePeriodIndexToTemplateId(request.getTimePeriod());
            if (templateId == null) {
                return Result.fail(400, "timePeriod不正确，应为1/2/3");
            }
            
            // target_sch_id 为空（调到空位置）
            targetSchId = null;

        } else if (request.getChangeType() == 1) {
            // 请假：需要 leaveTimeLength
            if (request.getLeaveTimeLength() == null || request.getLeaveTimeLength() <= 0) {
                return Result.fail(400, "请假需提供有效的leaveTimeLength");
            }
            leaveTimeLength = request.getLeaveTimeLength();
            
            // 请假不需要目标排班
            targetSchId = null;
            templateId = null;

        } else if (request.getChangeType() == 2) {
            // 与某医生换班：需要 targetDoctorId、targetDate 和 timePeriod
            if (!StringUtils.hasText(request.getTargetDoctorId()) || 
                request.getTargetDate() == null || 
                request.getTimePeriod() == null) {
                return Result.fail(400, "与医生换班需提供targetDoctorId、targetDate和timePeriod");
            }

            targetDate = request.getTargetDate();
            templateId = mapTimePeriodIndexToTemplateId(request.getTimePeriod());
            if (templateId == null) {
                return Result.fail(400, "timePeriod不正确，应为1/2/3");
            }

            // 查询目标医生的排班记录
            DocScheduleRecord targetSchedule = scheduleRecordMapper.findByDocAndDateAndPeriod(
                request.getTargetDoctorId(),
                targetDate,
                templateId
            );
            if (targetSchedule == null) {
                return Result.fail(404, "未找到目标医生的排班记录");
            }
            targetSchId = targetSchedule.getId();
        }

        // 6. 插入变更记录
        int affected = scheduleChangeRecordMapper.insertChangeRequest(
            request.getDocId(),
            originalSchedule.getId(),
            targetSchId,
            request.getReason(),
            "pending",
            targetDate,
            templateId,
            request.getChangeType(),
            leaveTimeLength
        );

        if (affected == 0) {
            return Result.fail(409, "插入变更记录失败，可能存在并发冲突");
        }

        // 7. 推送通知
        emitNotificationSnapshot(request.getDocId(), null);

        return Result.success(null, "班次变更申请已提交");
    }

    @Override
    @Transactional
    public Result<Void> updatePatientStatus(PatientStatusRequest request) {
        if (request == null || !StringUtils.hasText(request.getDoctorId()) || !StringUtils.hasText(request.getRegisterId())) {
            return Result.fail(400, "必填参数缺失");
        }

        RegisterIdUtil.RegisterKey key;
        try {
            key = RegisterIdUtil.decode(request.getRegisterId());
        } catch (IllegalArgumentException ex) {
            return Result.fail(400, "挂号id格式不正确");
        }

        // 获取排班记录及其时间模板信息
        com.example.dto.ScheduleDetailDto scheduleDetail = scheduleRecordMapper.getScheduleWithTemplateById(key.getScheduleId());
        if (scheduleDetail == null) {
            return Result.fail(404, "挂号记录不存在");
        }
        if (!request.getDoctorId().equals(scheduleDetail.getDocId())) {
            return Result.fail(403, "无权更新该挂号记录");
        }

        // 时间校验：检查当前时间是否在排班时段内
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        java.time.LocalTime currentTime = now.toLocalTime();

        // 检查日期
        if (scheduleDetail.getScheduleDate().isAfter(today)) {
            return Result.fail(400, "排班日期尚未到来，无法更新患者状态");
        }
        
        // 如果是当天，检查时间段
        if (scheduleDetail.getScheduleDate().isEqual(today)) {
            if (scheduleDetail.getStartTime() != null && scheduleDetail.getEndTime() != null) {
                if (currentTime.isBefore(scheduleDetail.getStartTime())) {
                    String timePeriodName = scheduleDetail.getTimePeriodName() != null ? 
                        scheduleDetail.getTimePeriodName() : "该时段";
                    return Result.fail(400, String.format("%s尚未开始（开始时间：%s），无法更新患者状态", 
                        timePeriodName, scheduleDetail.getStartTime()));
                }
                if (currentTime.isAfter(scheduleDetail.getEndTime())) {
                    String timePeriodName = scheduleDetail.getTimePeriodName() != null ? 
                        scheduleDetail.getTimePeriodName() : "该时段";
                    return Result.fail(400, String.format("%s已结束（结束时间：%s），无法更新患者状态", 
                        timePeriodName, scheduleDetail.getEndTime()));
                }
            }
        } else if (scheduleDetail.getScheduleDate().isBefore(today)) {
            // 如果是过去的日期，也视为已结束
            return Result.fail(400, "排班日期已过，无法更新患者状态");
        }

        // 使用 StatusConverter 工具类进行状态转换
        String patientStatusStr;
        String doctorStatusStr;
        try {
            patientStatusStr = com.example.utils.StatusConverter.convertPatientStatus(request.getPatientStatus());
            doctorStatusStr = com.example.utils.StatusConverter.convertDoctorStatus(request.getDoctorStatus());
        } catch (IllegalArgumentException ex) {
            return Result.fail(400, ex.getMessage());
        }

        int affected = registerRecordMapper.updateStatus(key.getPatientId(), key.getScheduleId(), patientStatusStr);
        if (affected == 0) {
            return Result.fail(500, "更新挂号状态失败");
        }

        int doctorAffected = doctorMapper.updateDoctorStatus(request.getDoctorId(), doctorStatusStr);
        if (doctorAffected == 0) {
            return Result.fail(500, "更新医生状态失败");
        }

        return Result.success(null, "状态已更新");
    }

    @Override
    @Transactional
    public Result<Void> updateDoctorProfile(String doctorId, DoctorProfileUpdateRequest profileData) {
        if (!StringUtils.hasText(doctorId)) {
            return Result.fail(400, "医生id不能为空");
        }
        if (profileData == null) {
            return Result.fail(400, "请求体不能为空");
        }
        Doctor doctor = doctorMapper.getDoctorWithDetails(doctorId);
        if (doctor == null) {
            return Result.fail(404, "医生不存在");
        }

        boolean hasUserFields = StringUtils.hasText(profileData.getName())
                || StringUtils.hasText(profileData.getEmail())
                || StringUtils.hasText(profileData.getPhone());
        boolean hasDoctorFields = StringUtils.hasText(profileData.getClinicId())
                || StringUtils.hasText(profileData.getTitleId());

        if (!hasUserFields && !hasDoctorFields) {
            return Result.fail(400, "未提供任何可更新字段");
        }

        if (hasUserFields) {
            doctorMapper.updateUserProfile(doctorId, profileData.getName(), profileData.getEmail(), profileData.getPhone());
        }
        if (hasDoctorFields) {
            doctorMapper.updateDoctorProfile(doctorId, profileData.getClinicId(), profileData.getTitleId());
        }

        return Result.success(null, "医生信息已更新");
    }

    private SseEmitter createEmitter(Map<String, SseEmitter> store, String key) {
        SseEmitter emitter = new SseEmitter(0L);
        store.put(key, emitter);
        emitter.onCompletion(() -> store.remove(key));
        emitter.onTimeout(() -> store.remove(key));
        sendEvent(store, key, emitter, "connected", "connected");
        return emitter;
    }

    private void emitAddNumberSnapshot(String docId, SseEmitter preferredEmitter) {
        SseEmitter emitter = preferredEmitter != null ? preferredEmitter : addNumberEmitters.get(docId);
        if (emitter == null) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("addApplications", buildAddNumberApplications(docId));
        sendEvent(addNumberEmitters, docId, emitter, "add-number-updated", Result.success(payload));
    }

    private void emitNotificationSnapshot(String docId, SseEmitter preferredEmitter) {
        SseEmitter emitter = preferredEmitter != null ? preferredEmitter : notificationEmitters.get(docId);
        if (emitter == null) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("notifications", loadSystemNotifications(docId));
        sendEvent(notificationEmitters, docId, emitter, "notification", Result.success(payload));
    }

    private void sendEvent(Map<String, SseEmitter> store, String key, SseEmitter emitter, String eventName, Object payload) {
        if (emitter == null) {
            return;
        }
        try {
            SseEmitter.SseEventBuilder builder = eventName == null
                    ? SseEmitter.event().data(payload)
                    : SseEmitter.event().name(eventName).data(payload);
            emitter.send(builder);
        } catch (IOException ex) {
            handleEmitterFailure(store, key, emitter, ex);
        }
    }

    private void handleEmitterFailure(Map<String, SseEmitter> store, String key, SseEmitter emitter, Exception ex) {
        store.remove(key);
        emitter.completeWithError(ex);
    }

    private List<AddNumberApplicationDto> buildAddNumberApplications(String docId) {
        List<AddNumberApplicationRow> rows = addNumberSourceRecordMapper.selectPendingApplicationRows(docId);
        return rows.stream()
                .map(this::mapAddNumberApplication)
                .collect(Collectors.toList());
    }

    private AddNumberApplicationDto mapAddNumberApplication(AddNumberApplicationRow row) {
        AddNumberApplicationDto dto = new AddNumberApplicationDto();
        dto.setAddId(RegisterIdUtil.encode(row.getPatientId(), row.getScheduleId()));
        dto.setPatientName(row.getPatientName());
        dto.setApplyTime(toOffsetDateTime(row.getApplyTime()));
        dto.setTargetDate(row.getScheduleDate());
        dto.setTargetTimePeriod(TimePeriodUtils.resolveTemplateIdToPeriodIndex(row.getTemplateId()));
        dto.setNote(row.getApplicationNote());
        return dto;
    }

    private DepartmentShiftDto mapDepartmentShift(DepartmentShiftRow row) {
        DepartmentShiftDto dto = new DepartmentShiftDto();
        dto.setDate(row.getScheduleDate());
        dto.setDocId(row.getDoctorId());
        dto.setDocName(row.getDoctorName());
        int period = row.getTemplateId() != null
                ? TimePeriodUtils.resolveTemplateIdToPeriodIndex(row.getTemplateId())
                : TimePeriodUtils.resolvePeriodIndex(row.getStartTime());
        dto.setTimePeriod(period);
        dto.setClinicPlace(row.getClinicNumber());
        return dto;
    }

    private SelfShiftDto mapSelfShift(SelfShiftRow row) {
        SelfShiftDto dto = new SelfShiftDto();
        dto.setDate(row.getScheduleDate());
        int period = row.getTemplateId() != null
                ? TimePeriodUtils.resolveTemplateIdToPeriodIndex(row.getTemplateId())
                : TimePeriodUtils.resolvePeriodIndex(row.getStartTime());
        dto.setTimePeriod(period);
        dto.setClinicPlace(row.getClinicNumber());
        return dto;
    }

    private PatientSummaryDto mapPatientSummary(PatientSummaryRow row) {
        PatientSummaryDto dto = new PatientSummaryDto();
        dto.setPatientName(row.getPatientName());
        dto.setRegisterId(RegisterIdUtil.encode(row.getPatientId(), row.getScheduleId()));
        dto.setGender(row.getGender());
        dto.setAge(calculateAge(row.getBirth()));
        dto.setScheduleDate(row.getScheduleDate());
        // 将 templateId 转换回编号返回给前端
        dto.setTimePeriod(TimePeriodUtils.resolveTemplateIdToPeriodIndex(row.getTemplateId()));
        return dto;
    }

    private PatientRecordDto mapPatientRecord(PatientRecordRow row) {
        PatientRecordDto dto = new PatientRecordDto();
        dto.setRegisterId(RegisterIdUtil.encode(row.getPatientId(), row.getScheduleId()));
        dto.setPatientId(row.getPatientId());
        dto.setRegisterTime(toOffsetDateTime(row.getRegisterTime()));
        dto.setDepartment(row.getDepartmentName());
        dto.setScheduleDate(row.getScheduleDate());
        return dto;
    }

    private List<NotificationMessageDto> loadSystemNotifications(String docId) {
        List<NotificationMessageDto> notifications = new ArrayList<>();
        for (ScheduleChangeRecordRow row : scheduleChangeRecordMapper.findByDoctor(docId)) {
            NotificationMessageDto dto = new NotificationMessageDto();
            dto.setId("schedule-change-" + row.getOriginalScheduleId() + "-" + row.getTargetScheduleId());
            dto.setTitle("班次变更申请状态");
            dto.setContent(String.format("原班次:%s 目标班次:%s 状态:%s",
                    row.getOriginalScheduleId(),
                    row.getTargetScheduleId(),
                    readableStatus(row.getStatus())));
            dto.setCreatedAt(OffsetDateTime.now(DEFAULT_ZONE));
            notifications.add(dto);
        }
        return notifications;
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(DEFAULT_ZONE).toOffsetDateTime();
    }

    private int calculateAge(LocalDate birth) {
        if (birth == null) {
            return 0;
        }
        return java.time.Period.between(birth, LocalDate.now()).getYears();
    }

    /**
     * 将时段编号映射为 schedule_template.id
     * 1 -> TIME0001, 2 -> TIME0002, 3 -> TIME0003
     */
    private String mapTimePeriodIndexToTemplateId(Integer index) {
        return TimePeriodUtils.mapPeriodIndexToTemplateId(index);
    }

    private String readableStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "pending";
        }
        switch (status.toLowerCase()) {
            case "已同意":
            case "approved":
                return "已同意";
            case "已拒绝":
            case "rejected":
                return "已拒绝";
            default:
                return status;
        }
    }
}
