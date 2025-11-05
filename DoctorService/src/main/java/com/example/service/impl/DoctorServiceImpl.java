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
import com.example.entity.Doctor;
import com.example.entity.DocScheduleRecord;
import com.example.entity.AddNumberSourceRecord;
import com.example.utils.*;
import com.example.mapper.DoctorMapper;
import com.example.mapper.DocScheduleRecordMapper;
import com.example.mapper.DocScheduleChangeRecordMapper;
import com.example.mapper.RegisterRecordMapper;
import com.example.mapper.AddNumberSourceRecordMapper;
import com.example.mapper.DocScheduleChangeRecordMapper.ScheduleChangeRecordRow;
import com.example.mapper.model.AddNumberApplicationRow;
import com.example.mapper.model.DepartmentShiftRow;
import com.example.mapper.model.PatientRecordRow;
import com.example.mapper.model.PatientSummaryRow;
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
    public Result<Void> reviewAddNumberRequest(AddNumberDecisionRequest request) {
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

        String status = request.isApproved() ? "approved" : "rejected";
        int affected = addNumberSourceRecordMapper.updateRequestStatus(key.getPatientId(), key.getScheduleId(), status);
        if (affected == 0) {
            return Result.fail(500, "更新加号申请失败");
        }

        emitAddNumberSnapshot(schedule.getDocId(), null);

        return Result.success(null, "审核完成");
    }

    @Override
    public List<DepartmentShiftDto> getDepartmentShifts(String docId) {
        List<DepartmentShiftRow> rows = scheduleRecordMapper.selectDepartmentShiftRows(docId, LocalDate.now());
        return rows.stream()
                .map(this::mapDepartmentShift)
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
        dto.setClinicId(doctor.getClinicId());
        dto.setTitle(StringUtils.hasText(doctor.getTitleName()) ? doctor.getTitleName() : "");
        return dto;
    }

    @Override
    @Transactional
    public Result<Void> submitScheduleChangeRequest(ScheduleChangeRequest request) {
        if (request == null || !StringUtils.hasText(request.getDocId()) || !StringUtils.hasText(request.getOriginalScheduleId())) {
            return Result.fail(400, "必填参数缺失");
        }

        DocScheduleRecord original = scheduleRecordMapper.getScheduleById(request.getOriginalScheduleId());
        if (original == null) {
            return Result.fail(404, "原班次不存在");
        }
        if (!request.getDocId().equals(original.getDocId())) {
            return Result.fail(403, "只能申请本人班次");
        }

        String targetScheduleId = original.getId();
        if (Integer.valueOf(0).equals(request.getChangeType())) {
            if (!StringUtils.hasText(request.getTargetDate()) || request.getTimePeriod() == null) {
                return Result.fail(400, "调班需提供目标日期与时段");
            }
            LocalDate targetDate;
            try {
                targetDate = LocalDate.parse(request.getTargetDate());
            } catch (Exception ex) {
                return Result.fail(400, "目标日期格式不正确");
            }
            String targetDoctorId = StringUtils.hasText(request.getTargetDoctorId()) ? request.getTargetDoctorId() : request.getDocId();
            String timePeriodName = mapTimePeriodName(request.getTimePeriod());
            DocScheduleRecord target = scheduleRecordMapper.findByDocAndDateAndPeriod(targetDoctorId, targetDate, timePeriodName);
            if (target == null) {
                return Result.fail(404, "未找到目标班次");
            }
            targetScheduleId = target.getId();
        }

        String reason = buildScheduleChangeReason(request);
        scheduleChangeRecordMapper.upsertChangeRequest(request.getDocId(), original.getId(), targetScheduleId, reason, "pending");

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

        DocScheduleRecord schedule = scheduleRecordMapper.getScheduleById(key.getScheduleId());
        if (schedule == null) {
            return Result.fail(404, "挂号记录不存在");
        }
        if (!request.getDoctorId().equals(schedule.getDocId())) {
            return Result.fail(403, "无权更新该挂号记录");
        }

        String patientStatus = mapPatientStatus(request.getPatientStatus());
        int affected = registerRecordMapper.updateStatus(key.getPatientId(), key.getScheduleId(), patientStatus);
        if (affected == 0) {
            return Result.fail(500, "更新挂号状态失败");
        }

        String doctorStatus = mapDoctorStatus(request.getDoctorStatus());
        doctorMapper.updateDoctorStatus(request.getDoctorId(), doctorStatus);

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
        dto.setTargetTimePeriod(TimePeriodUtils.resolvePeriodIndex(row.getTimePeriodName()));
        dto.setNote(row.getApplicationNote());
        return dto;
    }

    private DepartmentShiftDto mapDepartmentShift(DepartmentShiftRow row) {
        DepartmentShiftDto dto = new DepartmentShiftDto();
        dto.setDate(row.getScheduleDate());
        dto.setDocId(row.getDoctorId());
        dto.setDocName(row.getDoctorName());
        int period = row.getTimePeriodName() != null
                ? TimePeriodUtils.resolvePeriodIndex(row.getTimePeriodName())
                : TimePeriodUtils.resolvePeriodIndex(row.getStartTime());
        dto.setTimePeriod(period);
        return dto;
    }

    private PatientSummaryDto mapPatientSummary(PatientSummaryRow row) {
        PatientSummaryDto dto = new PatientSummaryDto();
        dto.setPatientName(row.getPatientName());
        dto.setRegisterId(RegisterIdUtil.encode(row.getPatientId(), row.getScheduleId()));
        dto.setGender(row.getGender());
        dto.setAge(calculateAge(row.getBirth()));
        dto.setScheduleDate(row.getScheduleDate());
        dto.setTimePeriod(TimePeriodUtils.resolvePeriodIndex(row.getTimePeriodName()));
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

    private String mapTimePeriodName(Integer index) {
        if (index == null) {
            return null;
        }
        switch (index) {
            case 1:
                return "上午";
            case 2:
                return "下午";
            case 3:
                return "晚上";
            default:
                return null;
        }
    }

    private String readableStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "pending";
        }
        switch (status.toLowerCase()) {
            case "approved":
                return "approved";
            case "rejected":
                return "rejected";
            default:
                return status;
        }
    }

    private String buildScheduleChangeReason(ScheduleChangeRequest request) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(request.getReason())) {
            builder.append(request.getReason());
        }
        if (request.getLeaveTimeLength() != null && request.getLeaveTimeLength() > 0) {
            if (builder.length() > 0) {
                builder.append("; ");
            }
            builder.append("请假时长:").append(request.getLeaveTimeLength()).append("天");
        }
        return builder.toString();
    }

    private String mapPatientStatus(Integer status) {
        if (status == null) {
            return "waiting";
        }
        switch (status) {
            case 1:
                return "in_progress";
            case 2:
                return "finished";
            default:
                return "waiting";
        }
    }

    private String mapDoctorStatus(Integer status) {
        if (status == null) {
            return "idle";
        }
        return status == 1 ? "consulting" : "idle";
    }
}