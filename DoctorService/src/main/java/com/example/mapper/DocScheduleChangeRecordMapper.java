package com.example.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DocScheduleChangeRecordMapper {

    @Insert({
        "INSERT INTO \"doc_schedule_change_record\" (\"doc_id\", \"ori_sch_id\", \"target_sch_id\", \"reason_text\", \"status\")",
        "VALUES (#{docId}, #{originalScheduleId}, #{targetScheduleId}, #{reason}, #{status})",
        "ON CONFLICT (\"doc_id\", \"ori_sch_id\", \"target_sch_id\")",
        "DO UPDATE SET \"reason_text\" = EXCLUDED.\"reason_text\", \"status\" = EXCLUDED.\"status\""
    })
    int upsertChangeRequest(@Param("docId") String docId,
                            @Param("originalScheduleId") String originalScheduleId,
                            @Param("targetScheduleId") String targetScheduleId,
                            @Param("reason") String reason,
                            @Param("status") String status);

    @Select({
        "SELECT",
        "  \"doc_id\"    AS docId,",
        "  \"ori_sch_id\" AS originalScheduleId,",
        "  \"target_sch_id\" AS targetScheduleId,",
        "  \"status\"    AS status",
        "FROM \"doc_schedule_change_record\"",
        "WHERE \"doc_id\" = #{docId}"
    })
    List<ScheduleChangeRecordRow> findByDoctor(@Param("docId") String docId);

    class ScheduleChangeRecordRow {
        private String docId;
        private String originalScheduleId;
        private String targetScheduleId;
        private String status;

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public String getOriginalScheduleId() {
            return originalScheduleId;
        }

        public void setOriginalScheduleId(String originalScheduleId) {
            this.originalScheduleId = originalScheduleId;
        }

        public String getTargetScheduleId() {
            return targetScheduleId;
        }

        public void setTargetScheduleId(String targetScheduleId) {
            this.targetScheduleId = targetScheduleId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
