package com.example.service.impl;

import com.example.dto.DoctorLoginRequest;
import com.example.dto.PatientStatusRequest;
import com.example.dto.ScheduleChangeRequest;
import com.example.entity.Doctor;
import com.example.utils.*;
import com.example.mapper.DoctorMapper;
import com.example.mapper.DocScheduleRecordMapper;
import com.example.mapper.RegisterRecordMapper;
import com.example.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
    // Service 层只关注业务结果，可以返回一个封装了状态的对象，或者抛出业务异常。
    public Result<Map<String, String>> login(DoctorLoginRequest request){
        // 1. 检查医生ID和密码是否为空 (业务输入验证)
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
            System.out.println("医生登录失败，医生ID: " + doctor.getId() + ", 输入密码错误");
            return Result.fail(401, "医生账号或密码错误");
        }
        
        // 5. 生成JWT令牌 (业务功能实现)
        try {
            String jwtToken = JwtUtil.generateToken(doctor.getId());
            System.out.println("医生登录成功，医生ID: " + doctor.getId() + ", 生成的JWT: " + jwtToken);
            return Result.loginSuccess(doctor.getId(),jwtToken);
            
        } catch (Exception e) {
            // JWT生成失败
            return Result.fail(500, "系统错误，令牌生成失败");
        }
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