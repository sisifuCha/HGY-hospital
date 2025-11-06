package com.example.mapper;

import com.example.mapper.model.PatientRecordRow;
import com.example.mapper.model.PatientSummaryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RegisterRecordMapper {
       @Select({
              "SELECT",
              "  rr.\"patient_id\" AS patientId,",
              "  rr.\"sch_id\"     AS scheduleId,",
              "  rr.register_time    AS registerTime,",
              "  u.\"name\"          AS patientName,",
              "  u.\"sex\"           AS gender,",
              "  p.birth              AS birth,",
              "  dsr.schedule_date    AS scheduleDate,",
              "  st.time_period_name  AS timePeriodName",
              "FROM \"register_record\" rr",
              "JOIN \"doc_schedule_record\" dsr ON rr.\"sch_id\" = dsr.\"id\"",
              "LEFT JOIN \"schedule_template\" st ON dsr.template_id = st.id",
              "JOIN \"patient\" p ON rr.\"patient_id\" = p.\"id\"",
              "JOIN \"user\" u ON p.\"id\" = u.\"id\"",
              "WHERE dsr.\"doc_id\" = #{docId} AND rr.\"status\" != 'cancelled'",
              "ORDER BY dsr.schedule_date, st.start_time NULLS LAST, u.\"name\""
       })
       List<PatientSummaryRow> selectPatientSummaryRows(@Param("docId") String docId);

       @Select({
              "SELECT",
              "  rr.\"patient_id\" AS patientId,",
              "  rr.\"sch_id\"     AS scheduleId,",
              "  rr.register_time    AS registerTime,",
              "  dep.\"name\"        AS departmentName,",
              "  dsr.schedule_date   AS scheduleDate",
              "FROM \"register_record\" rr",
              "JOIN \"doc_schedule_record\" dsr ON rr.\"sch_id\" = dsr.\"id\"",
              "JOIN \"doctor\" d ON dsr.\"doc_id\" = d.\"id\"",
              "JOIN \"clinic\" c ON d.\"clinic_id\" = c.\"id\"",
              "JOIN \"department\" dep ON c.\"dep_id\" = dep.\"id\"",
              "WHERE rr.\"patient_id\" = #{patientId}",
              "ORDER BY rr.register_time DESC"
       })
       List<PatientRecordRow> selectPatientHistoryRows(@Param("patientId") String patientId);

    @Update("UPDATE \"register_record\" SET \"status\" = #{status} " +
           "WHERE \"patient_id\" = #{patientId} AND \"sch_id\" = #{schId}")
    int updateStatus(@Param("patientId") String patientId, 
                    @Param("schId") String schId, 
                    @Param("status") String status);
}