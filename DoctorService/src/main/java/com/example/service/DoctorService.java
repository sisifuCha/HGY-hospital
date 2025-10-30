package com.example.service;
import com.example.dto.DoctorLoginRequest;
import com.example.dto.PatientStatusRequest;
import com.example.dto.ScheduleChangeRequest;
import com.example.utils.Result;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface DoctorService {
    // 医生登录
    Result<String> login(DoctorLoginRequest request);

    // 获取加号请求通知
    SseEmitter getAddNumberNotifications(String docID);

    // 审核加号请求
    boolean reviewAddNumberRequest(String addNumberId, boolean approved);

    // 获取本科室排班列表
    Object getDepartmentShifts(String docID);

    // 获取患者列表
    Object getPatientList(String docID);

    // 获取特定患者信息
    Object getPatientDetails(String docID, String registerId);

    // 获取系统通知
    SseEmitter getSystemNotifications(String docID);

    // 获取医生个人信息
    Object getDoctorProfile(String docID);

    // 提交班次变更申请
    boolean submitScheduleChangeRequest(ScheduleChangeRequest request);

    // 更新患者就诊状态
    boolean updatePatientStatus(PatientStatusRequest request);

    // 修改医生个人信息
    boolean updateDoctorProfile(String doctorId, Object profileData);
}