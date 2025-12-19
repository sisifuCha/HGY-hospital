package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.dto.AddNumberStatusDto;
import com.example.pojo.entity.AddNumberSourceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 加号申请记录 Mapper 接口
 */
@Mapper
public interface AddNumberSourceRecordMapper extends BaseMapper<AddNumberSourceRecord> {

    /**
     * 查询患者的加号申请记录（带详细信息）
     */
    @Select("SELECT " +
            "    ans.patient_id, " +
            "    ans.sch_id AS schedule_record_id, " +
            "    ans.status, " +
            "    ans.apply_time, " +
            "    ans.reason_text, " +
            "    ans.reason_pic, " +
            "    d.name AS doctor_name, " +
            "    dept.name AS department_name, " +
            "    dsr.date AS schedule_date, " +
            "    tp.name AS time_period_name " +
            "FROM add_number_source_record ans " +
            "INNER JOIN doc_schedule_record dsr ON ans.sch_id = dsr.id " +
            "INNER JOIN doctor d ON dsr.doc_id = d.id " +
            "INNER JOIN department dept ON d.dep_id = dept.id " +
            "INNER JOIN time_period tp ON dsr.time_period_id = tp.id " +
            "WHERE ans.patient_id = #{patientId} " +
            "ORDER BY ans.apply_time DESC")
    List<AddNumberStatusDto> selectPatientAddNumberHistory(@Param("patientId") String patientId);

    /**
     * 查询单条加号申请详情
     */
    @Select("SELECT " +
            "    ans.patient_id, " +
            "    ans.sch_id AS schedule_record_id, " +
            "    ans.status, " +
            "    ans.apply_time, " +
            "    ans.reason_text, " +
            "    ans.reason_pic, " +
            "    d.name AS doctor_name, " +
            "    dept.name AS department_name, " +
            "    dsr.date AS schedule_date, " +
            "    tp.name AS time_period_name " +
            "FROM add_number_source_record ans " +
            "INNER JOIN doc_schedule_record dsr ON ans.sch_id = dsr.id " +
            "INNER JOIN doctor d ON dsr.doc_id = d.id " +
            "INNER JOIN department dept ON d.dep_id = dept.id " +
            "INNER JOIN time_period tp ON dsr.time_period_id = tp.id " +
            "WHERE ans.patient_id = #{patientId} AND ans.sch_id = #{scheduleRecordId}")
    AddNumberStatusDto selectAddNumberDetail(@Param("patientId") String patientId, 
                                             @Param("scheduleRecordId") String scheduleRecordId);
}
