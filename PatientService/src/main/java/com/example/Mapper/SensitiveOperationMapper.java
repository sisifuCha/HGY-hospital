package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.SensitiveOperation;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 敏感操作Mapper
 */
@Mapper
public interface SensitiveOperationMapper extends BaseMapper<SensitiveOperation> {

    /**
     * 查询患者的所有敏感操作记录
     */
    @Select("SELECT * FROM sensitive_operation WHERE patient_id = #{patientId} ORDER BY op_time DESC")
    List<SensitiveOperation> findByPatientId(@Param("patientId") String patientId);

    /**
     * 统计患者的敏感操作次数
     */
    @Select("SELECT COUNT(*) FROM sensitive_operation WHERE patient_id = #{patientId}")
    int countByPatientId(@Param("patientId") String patientId);

    /**
     * 查询患者最新的敏感操作记录
     */
    @Select("SELECT * FROM sensitive_operation WHERE patient_id = #{patientId} ORDER BY op_time DESC LIMIT 1")
    SensitiveOperation findLatestByPatientId(@Param("patientId") String patientId);

    /**
     * 更新患者所有敏感操作的时间（统一更新）
     */
    @Update("UPDATE sensitive_operation SET op_time = #{newTime} WHERE patient_id = #{patientId}")
    int updateAllOpTimeByPatientId(@Param("patientId") String patientId, @Param("newTime") LocalDateTime newTime);

    /**
     * 查询所有20天前的敏感操作记录（用于清理）
     */
    @Select("SELECT DISTINCT patient_id FROM sensitive_operation WHERE op_time < #{beforeTime}")
    List<String> findExpiredPatientIds(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 删除患者的所有敏感操作记录
     */
    @Delete("DELETE FROM sensitive_operation WHERE patient_id = #{patientId}")
    int deleteByPatientId(@Param("patientId") String patientId);

    /**
     * 查询患者最近3条敏感操作记录ID
     */
    @Select("SELECT id FROM sensitive_operation WHERE patient_id = #{patientId} ORDER BY op_time DESC LIMIT 3")
    List<String> findTop3IdsByPatientId(@Param("patientId") String patientId);

    /**
     * 生成新的敏感操作ID
     */
    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(id, 4) AS INTEGER)), 0) + 1 FROM sensitive_operation WHERE id LIKE 'SEN%'")
    Integer generateNextId();
}
