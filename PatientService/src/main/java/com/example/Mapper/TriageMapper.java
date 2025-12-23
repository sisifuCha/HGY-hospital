package com.example.Mapper;

import com.example.dto.TriageRecordRow;
import com.example.pojo.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TriageMapper {
    int insertTriageRecord(@Param("record") TriageRecordRow record);

    TriageRecordRow selectByTriageId(@Param("triageId") String triageId);

    List<TriageRecordRow> fetchHistory(@Param("patientId") String patientId);

    /**
     * 获取所有子科室（用于导诊匹配）
     */
    List<Department> findAllSubDepartments();
}
