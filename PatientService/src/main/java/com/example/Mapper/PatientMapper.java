package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.dto.PatientProfileDto;
import com.example.pojo.entity.Patient;
import com.example.pojo.vo.PatientProfileVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 患者数据访问层
 * 继承自 Mybatis-Plus 的 BaseMapper，提供了基础的 CRUD 功能
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
    @Select("SELECT count(*) FROM \"patient\" WHERE identification_id = #{identificationId}")
    int countByIdentificationId(String identificationId);

    int updateProfile(@Param("patientId") String patientId, @Param("profile") PatientProfileDto profile);

    PatientProfileVo findProfileById(@Param("patientId") String patientId);
}
