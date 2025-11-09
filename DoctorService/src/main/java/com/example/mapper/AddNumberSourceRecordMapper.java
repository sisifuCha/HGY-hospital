package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.AddNumberSourceRecord;
import com.example.mapper.model.AddNumberApplicationRow;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddNumberSourceRecordMapper extends BaseMapper<AddNumberSourceRecord> {
    @Select("SELECT * FROM \"add_number_source_record\" WHERE \"sch_id\" IN " +
           "(SELECT \"id\" FROM \"doc_schedule_record\" WHERE \"doc_id\" = #{docId}) " +
           "AND \"status\" = 'pending'")
    List<AddNumberSourceRecord> getPendingRequests(@Param("docId") String docId);

       @Select({
              "SELECT",
              "  ans.patient_id   AS patientId,",
              "  ans.sch_id       AS scheduleId,",
              "  ans.apply_time   AS applyTime,",
              "  ans.reason_text  AS applicationNote,",
              "  u.\"name\"       AS patientName,",
              "  dsr.schedule_date AS scheduleDate,",
              "  dsr.template_id  AS templateId",
              "FROM \"add_number_source_record\" ans",
              "JOIN \"patient\" p ON ans.patient_id = p.\"id\"",
              "JOIN \"user\" u ON p.\"id\" = u.\"id\"",
              "JOIN \"doc_schedule_record\" dsr ON ans.sch_id = dsr.\"id\"",
              "WHERE dsr.\"doc_id\" = #{docId} AND ans.\"status\" = 'pending'",
              "ORDER BY ans.apply_time DESC"
       })
       List<AddNumberApplicationRow> selectPendingApplicationRows(@Param("docId") String docId);

    @Update("UPDATE \"add_number_source_record\" SET \"status\" = #{status} " +
           "WHERE \"patient_id\" = #{patientId} AND \"sch_id\" = #{schId}")
    int updateRequestStatus(@Param("patientId") String patientId, 
                          @Param("schId") String schId, 
                          @Param("status") String status);

    @Select("SELECT * FROM \"add_number_source_record\" " +
           "WHERE \"patient_id\" = #{patientId} AND \"sch_id\" = #{schId}")
    AddNumberSourceRecord getRequest(@Param("patientId") String patientId, 
                                   @Param("schId") String schId);
}