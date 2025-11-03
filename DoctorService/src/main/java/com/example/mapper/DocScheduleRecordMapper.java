package com.example.mapper;

import com.example.entity.DocScheduleRecord;
import com.example.mapper.model.DepartmentShiftRow;
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
           "JOIN \"doctor\" d ON dsr.\"doc_id\" = d.\"id\" " +
           "JOIN \"clinic\" c ON d.\"clinic_id\" = c.\"id\" " +
           "JOIN \"department\" dep ON c.\"dep_id\" = dep.\"id\" " +
           "WHERE dep.\"id\" = (SELECT c2.\"dep_id\" FROM \"clinic\" c2 " +
           "JOIN \"doctor\" d2 ON c2.\"id\" = d2.\"clinic_id\" " +
           "WHERE d2.\"id\" = #{docId}) " +
           "AND dsr.\"schedule_date\" >= #{date}")
    List<DocScheduleRecord> getDepartmentSchedules(@Param("docId") String docId, @Param("date") Date date);

    @Select("SELECT * FROM \"doc_schedule_record\" WHERE \"id\" = #{scheduleId}")
    DocScheduleRecord getScheduleById(@Param("scheduleId") String scheduleId);

    @Select({
        "SELECT",
        "  dsr.schedule_date   AS scheduleDate,",
        "  doc_user.\"name\"   AS doctorName,",
        "  d.\"id\"           AS doctorId,",
        "  st.time_period_name AS timePeriodName,",
        "  st.start_time       AS startTime",
        "FROM \"doc_schedule_record\" dsr",
        "JOIN \"doctor\" d ON dsr.\"doc_id\" = d.\"id\"",
        "JOIN \"user\" doc_user ON d.\"id\" = doc_user.\"id\"",
        "JOIN \"clinic\" c ON d.\"clinic_id\" = c.\"id\"",
        "JOIN \"department\" dep ON c.\"dep_id\" = dep.\"id\"",
        "LEFT JOIN \"schedule_template\" st ON dsr.template_id = st.id",
        "WHERE dep.\"id\" = (",
        "    SELECT c2.\"dep_id\"",
        "    FROM \"clinic\" c2",
        "    JOIN \"doctor\" d2 ON c2.\"id\" = d2.\"clinic_id\"",
        "    WHERE d2.\"id\" = #{docId}",
        ")",
        "  AND dsr.schedule_date >= #{fromDate}",
        "ORDER BY dsr.schedule_date, st.start_time NULLS LAST, doc_user.\"name\""
    })
    List<DepartmentShiftRow> selectDepartmentShiftRows(@Param("docId") String docId, @Param("fromDate") LocalDate fromDate);

    @Select({
        "SELECT dsr.*",
        "FROM \"doc_schedule_record\" dsr",
        "LEFT JOIN \"schedule_template\" st ON dsr.template_id = st.id",
        "WHERE dsr.\"doc_id\" = #{docId}",
        "  AND dsr.schedule_date = #{date}",
        "  AND (st.time_period_name = #{timePeriodName} OR #{timePeriodName} IS NULL)",
        "ORDER BY dsr.schedule_date",
        "LIMIT 1"
    })
    DocScheduleRecord findByDocAndDateAndPeriod(@Param("docId") String docId,
                                                @Param("date") LocalDate date,
                                                @Param("timePeriodName") String timePeriodName);
}