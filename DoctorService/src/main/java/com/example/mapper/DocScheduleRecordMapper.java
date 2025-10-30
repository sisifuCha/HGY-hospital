package com.example.mapper;

import com.example.entity.DocScheduleRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface DocScheduleRecordMapper {
    @Select("SELECT * FROM \"doc_schedule_record\" WHERE \"doc_ID\" = #{docId} AND \"schedule_date\" >= #{date}")
    List<DocScheduleRecord> getDocSchedules(@Param("docId") String docId, @Param("date") Date date);

    @Select("SELECT dsr.* FROM \"doc_schedule_record\" dsr " +
           "JOIN \"doctor\" d ON dsr.\"doc_ID\" = d.\"ID\" " +
           "JOIN \"clinic\" c ON d.\"clinic_ID\" = c.\"ID\" " +
           "JOIN \"department\" dep ON c.\"dep_ID\" = dep.\"ID\" " +
           "WHERE dep.\"ID\" = (SELECT c2.\"dep_ID\" FROM \"clinic\" c2 " +
           "JOIN \"doctor\" d2 ON c2.\"ID\" = d2.\"clinic_ID\" " +
           "WHERE d2.\"ID\" = #{docId}) " +
           "AND dsr.\"schedule_date\" >= #{date}")
    List<DocScheduleRecord> getDepartmentSchedules(@Param("docId") String docId, @Param("date") Date date);

    @Select("SELECT * FROM \"doc_schedule_record\" WHERE \"ID\" = #{scheduleId}")
    DocScheduleRecord getScheduleById(@Param("scheduleId") String scheduleId);
}