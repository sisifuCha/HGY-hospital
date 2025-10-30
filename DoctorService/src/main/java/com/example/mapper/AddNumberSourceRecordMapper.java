package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.AddNumberSourceRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddNumberSourceRecordMapper extends BaseMapper<AddNumberSourceRecord> {
    @Select("SELECT * FROM \"add_number_source_record\" WHERE \"sch_ID\" IN " +
           "(SELECT \"ID\" FROM \"doc_schedule_record\" WHERE \"doc_ID\" = #{docId}) " +
           "AND \"status\" = 'pending'")
    List<AddNumberSourceRecord> getPendingRequests(@Param("docId") String docId);

    @Update("UPDATE \"add_number_source_record\" SET \"status\" = #{status} " +
           "WHERE \"patient_ID\" = #{patientId} AND \"sch_ID\" = #{schId}")
    int updateRequestStatus(@Param("patientId") String patientId, 
                          @Param("schId") String schId, 
                          @Param("status") String status);

    @Select("SELECT * FROM \"add_number_source_record\" " +
           "WHERE \"patient_ID\" = #{patientId} AND \"sch_ID\" = #{schId}")
    AddNumberSourceRecord getRequest(@Param("patientId") String patientId, 
                                   @Param("schId") String schId);
}