package com.example.Service;

import com.example.pojo.dto.PatientPageRequest;
import com.example.pojo.vo.PatientDetailVO;
import com.example.Conmon.result.Result;

import java.util.List;

public interface PatientService {
    /**
     * 分页获取患者列表
     * @param pageRequest 分页请求参数
     * @return 分页结果
     */
    Result<List<PatientDetailVO>> getPatientList(PatientPageRequest pageRequest);

    /**
     * 根据ID获取患者详情
     * @param id 患者ID
     * @return 患者详情
     */
    Result<PatientDetailVO> getPatientById(String id);

    /**
     * 更新患者信息
     * @param id 患者ID
     * @param dto 患者信息
     * @return 更新结果
     */
    Result<?> updatePatient(String id, com.example.pojo.dto.PatientDTO dto);
}