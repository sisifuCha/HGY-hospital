package com.example.doctor.service.impl;

import com.example.doctor.dto.DoctorLoginRequest;
import com.example.doctor.dto.PatientStatusRequest;
import com.example.doctor.dto.ScheduleChangeRequest;
import com.example.doctor.entity.Doctor;
import com.example.doctor.mapper.DoctorMapper;
import com.example.doctor.mapper.DocScheduleRecordMapper;
import com.example.doctor.mapper.RegisterRecordMapper;
import com.example.doctor.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DoctorServiceImpl implements DoctorService {
    
    @Autowired
    private DoctorMapper doctorMapper;
    
    @Autowired
    private DocScheduleRecordMapper scheduleRecordMapper;
    
    @Autowired
    private RegisterRecordMapper registerRecordMapper;

    // 存储SSE连接
    private final ConcurrentHashMap<String, SseEmitter> addNumberEmitters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SseEmitter> notificationEmitters = new ConcurrentHashMap<>();

    @Override
    public String login(DoctorLoginRequest request) {
        // 实现登录逻辑，生成token
        Doctor doctor = doctorMapper.getDoctorById(request.getDocID());
        if (doctor != null) {
            // TODO: 验证密码，生成token
            return "mock-token-" + request.getDocID();
        }
        return null;
    }

    @Override
    public SseEmitter getAddNumberNotifications(String docId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        addNumberEmitters.put(docId, emitter);
        
        emitter.onCompletion(() -> addNumberEmitters.remove(docId));
        emitter.onTimeout(() -> addNumberEmitters.remove(docId));
        
        // 发送初始数据
        try {
            emitter.send(SseEmitter.event().data("Connected"));
        } catch (Exception e) {
            // 处理异常
        }
        
        return emitter;
    }

    @Override
    public boolean reviewAddNumberRequest(String addNumberId, boolean approved) {
        // TODO: 实现审核加号请求逻辑
        return true;
    }

    @Override
    public Object getDepartmentShifts(String docId) {
        return scheduleRecordMapper.getDepartmentSchedules(docId, new Date());
    }

    @Override
    public Object getPatientList(String docId) {
        return registerRecordMapper.getPatientList(docId);
    }

    @Override
    public Object getPatientDetails(String docId, String registerId) {
        // TODO: 实现获取患者详细信息逻辑
        return null;
    }

    @Override
    public SseEmitter getSystemNotifications(String docId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        notificationEmitters.put(docId, emitter);
        
        emitter.onCompletion(() -> notificationEmitters.remove(docId));
        emitter.onTimeout(() -> notificationEmitters.remove(docId));
        
        try {
            emitter.send(SseEmitter.event().data("Connected"));
        } catch (Exception e) {
            // 处理异常
        }
        
        return emitter;
    }

    @Override
    public Object getDoctorProfile(String docId) {
        return doctorMapper.getDoctorWithDetails(docId);
    }

    @Override
    public boolean submitScheduleChangeRequest(ScheduleChangeRequest request) {
        // TODO: 实现提交班次变更申请逻辑
        return true;
    }

    @Override
    public boolean updatePatientStatus(PatientStatusRequest request) {
        // TODO: 实现更新患者就诊状态逻辑
        return true;
    }

    @Override
    public boolean updateDoctorProfile(String doctorId, Object profileData) {
        // TODO: 实现更新医生个人信息逻辑
        return true;
    }
}