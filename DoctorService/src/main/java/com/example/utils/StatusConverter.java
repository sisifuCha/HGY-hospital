package com.example.utils;

public class StatusConverter {
    // 医生状态：0-空闲(在职)，1-坐诊(就诊中)
    public static String convertDoctorStatus(Integer code) {
        if (code == null) return null;
        switch (code) {
            case 0: return "在职";
            case 1: return "就诊中";
            default: throw new IllegalArgumentException("无效医生状态代码: " + code);
        }
    }
    // 挂号单状态：0-已挂号，1-就诊中，2-已就诊
    public static String convertPatientStatus(Integer code) {
        if (code == null) return null;
        switch (code) {
            case 0: return "已挂号";
            case 1: return "就诊中";
            case 2: return "已就诊";
            default: throw new IllegalArgumentException("无效挂号单状态代码: " + code);
        }
    }
}
