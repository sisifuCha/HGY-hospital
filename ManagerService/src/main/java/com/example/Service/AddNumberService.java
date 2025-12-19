package com.example.Service;

import com.example.Conmon.result.Result;
import java.util.Map;

public interface AddNumberService {
    // 获取加号请求列表
    Result<Map<String, Object>> getAddedSource(Integer pageSize, Integer page);
    
    // 审批加号请求
    Result<String> checkAddedSource(String patientId, String schId, String status);
}