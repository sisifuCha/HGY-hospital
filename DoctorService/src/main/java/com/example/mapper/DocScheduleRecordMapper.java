package com.example.mapper;

import com.example.entity.DocScheduleRecord;
import com.example.mapper.model.DepartmentShiftRow;
import com.example.mapper.model.SelfShiftRow;
import java.time.LocalDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface DocScheduleRecordMapper {
    @Select("SELECT * FROM \"doc_schedule_record\" WHERE \"doc_id\" = #{docId} AND \"schedule_date\" >= #{date}")
    List<DocScheduleRecord> getDocSchedules(@Param("docId") String docId, @Param("date") Date date);

    @Select("SELECT dsr.* FROM \"doc_schedule_record\" dsr " +
           "JOIN \"clinic\" c ON dsr.\"clinic_id\" = c.\"id\" " +
           "JOIN \"department\" dep ON c.\"dep_id\" = dep.\"id\" " +
           "WHERE dep.\"id\" = (" +
           "    SELECT c2.\"dep_id\" " +
           "    FROM \"doc_schedule_record\" dsr2 " +
           "    JOIN \"clinic\" c2 ON dsr2.\"clinic_id\" = c2.\"id\" " +
           "    WHERE dsr2.\"doc_id\" = #{docId} " +
           "    LIMIT 1" +
           ") " +
           "AND dsr.\"schedule_date\" >= #{date}")
    List<DocScheduleRecord> getDepartmentSchedules(@Param("docId") String docId, @Param("date") Date date);

    @Select("SELECT * FROM \"doc_schedule_record\" WHERE \"id\" = #{scheduleId}")
    DocScheduleRecord getScheduleById(@Param("scheduleId") String scheduleId);

    @Select({
        "SELECT",
        "  dsr.schedule_date   AS scheduleDate,",
        "  doc_user.\"name\"   AS doctorName,",
        "  d.\"id\"           AS doctorId,",
        "  dsr.template_id     AS templateId,",
        "  st.start_time       AS startTime,",
        "  c.\"clinic_number\" AS clinicNumber",
        "FROM \"doc_schedule_record\" dsr",
        "JOIN \"doctor\" d ON dsr.\"doc_id\" = d.\"id\"",
        "JOIN \"user\" doc_user ON d.\"id\" = doc_user.\"id\"",
        "JOIN \"clinic\" c ON dsr.\"clinic_id\" = c.\"id\"",
        "JOIN \"department\" dep ON c.\"dep_id\" = dep.\"id\"",
        "LEFT JOIN \"schedule_template\" st ON dsr.template_id = st.id",
        "WHERE dep.\"id\" = (",
        "    SELECT c2.\"dep_id\"",
        "    FROM \"clinic\" c2",
        "    JOIN \"doc_schedule_record\" dsr2 ON c2.\"id\" = dsr2.\"clinic_id\"",
        "    WHERE dsr2.\"doc_id\" = #{docId}",
        "    LIMIT 1",
        ")",
        "  AND dsr.schedule_date >= #{fromDate}",
        "ORDER BY dsr.schedule_date, st.start_time NULLS LAST, doc_user.\"name\""
    })
    List<DepartmentShiftRow> selectDepartmentShiftRows(@Param("docId") String docId, @Param("fromDate") LocalDate fromDate);

    @Select({
        "SELECT",
        "  dsr.schedule_date   AS scheduleDate,",
        "  dsr.template_id     AS templateId,",
        "  st.start_time       AS startTime,",
        "  c.\"clinic_number\" AS clinicNumber",
        "FROM \"doc_schedule_record\" dsr",
        "JOIN \"clinic\" c ON dsr.\"clinic_id\" = c.\"id\"",
        "LEFT JOIN \"schedule_template\" st ON dsr.template_id = st.id",
        "WHERE dsr.\"doc_id\" = #{docId}",
        "  AND dsr.schedule_date >= #{fromDate}",
        "ORDER BY dsr.schedule_date, st.start_time NULLS LAST"
    })
    List<SelfShiftRow> selectSelfShiftRows(@Param("docId") String docId, @Param("fromDate") LocalDate fromDate);

    @Select({
        "SELECT dsr.*",
        "FROM \"doc_schedule_record\" dsr",
        "WHERE dsr.\"doc_id\" = #{docId}",
        "  AND dsr.schedule_date = #{date}",
        "  AND (dsr.template_id = #{templateId} OR #{templateId} IS NULL)",
        "ORDER BY dsr.schedule_date",
        "LIMIT 1"
    })
    DocScheduleRecord findByDocAndDateAndPeriod(@Param("docId") String docId,
                                                @Param("date") LocalDate date,
                                                @Param("templateId") String templateId);

    @Select({
        "SELECT",
        "  dsr.id AS scheduleId,",
        "  dsr.schedule_date AS scheduleDate,",
        "  dsr.template_id AS templateId,",
        "  dsr.doc_id AS docId,",
        "  st.start_time AS startTime,",
        "  st.end_time AS endTime,",
        "  st.time_period_name AS timePeriodName",
        "FROM \"doc_schedule_record\" dsr",
        "LEFT JOIN \"schedule_template\" st ON dsr.template_id = st.id",
        "WHERE dsr.id = #{scheduleId}"
    })
    com.example.dto.ScheduleDetailDto getScheduleWithTemplateById(@Param("scheduleId") String scheduleId);
}