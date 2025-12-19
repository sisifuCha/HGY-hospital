package com.example.Service;

import com.example.pojo.dto.AddNumberRequest;
import com.example.pojo.dto.AddNumberStatusDto;

import java.util.List;

/**
 * 加号申请服务接口
 */
public interface AddNumberService {

    /**
     * 提交加号申请
     * @param request 加号申请请求
     * @return 加号申请状态
     */
    AddNumberStatusDto submitAddNumberRequest(AddNumberRequest request);

    /**
     * 查询加号申请状态
     * @param patientId 患者ID
     * @param scheduleRecordId 排班记录ID
     * @return 加号申请状态
     */
    AddNumberStatusDto getAddNumberStatus(String patientId, String scheduleRecordId);

    /**
     * 查询患者的加号申请历史
     * @param patientId 患者ID
     * @return 加号申请历史列表
     */
    List<AddNumberStatusDto> getAddNumberHistory(String patientId);
}
