package com.example.service;
import com.example.dto.AddNumberDecisionRequest;
import com.example.dto.DepartmentShiftDto;
import com.example.dto.DoctorLoginRequest;
import com.example.dto.DoctorProfileDto;
import com.example.dto.DoctorProfileUpdateRequest;
import com.example.dto.PatientRecordDto;
import com.example.dto.PatientStatusRequest;
import com.example.dto.PatientSummaryDto;
import com.example.dto.ScheduleChangeRequest;
import com.example.dto.SelfShiftDto;
import com.example.utils.Result;

import java.util.List;
import java.util.Map;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface DoctorService {
    // 医生登录
    Result<Map<String,String>> login(DoctorLoginRequest request);

    // 获取加号请求通知
    SseEmitter getAddNumberNotifications(String docId);

    // 审核加号请求，返回包含后续操作码的响应数据
    Result<java.util.Map<String, String>> reviewAddNumberRequest(AddNumberDecisionRequest request);

    // 获取本科室排班列表
    List<DepartmentShiftDto> getDepartmentShifts(String docId);

    // 获取当前医生排班列表
    List<SelfShiftDto> getSelfShifts(String docId);

    // 获取患者列表
    List<PatientSummaryDto> getPatientList(String docId);

    // 获取特定患者信息
    List<PatientRecordDto> getPatientDetails(String docId, String registerId);

    // 获取系统通知
    SseEmitter getSystemNotifications(String docId);

    // 获取医生个人信息
    DoctorProfileDto getDoctorProfile(String docId);

    // 提交班次变更申请
    Result<Void> submitScheduleChangeRequest(ScheduleChangeRequest request);

    // 更新患者就诊状态
    Result<Void> updatePatientStatus(PatientStatusRequest request);

    // 修改医生个人信息
    Result<Void> updateDoctorProfile(String doctorId, DoctorProfileUpdateRequest profileData);
}