package com.example.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.pojo.dto.PatientDTO;
import com.example.pojo.vo.PatientDetailVO;
import com.example.Conmon.result.Result;

public interface PatientService {
    /**
     * 分页获取患者列表
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Result<Page<PatientDetailVO>> getPatientListWithPlus(Integer pageNum, Integer pageSize);

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
    Result<?> updatePatient(String id, PatientDTO dto);
}