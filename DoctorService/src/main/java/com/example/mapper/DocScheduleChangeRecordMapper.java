package com.example.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DocScheduleChangeRecordMapper {

    @Insert({
        "INSERT INTO \"doc_schedule_change_record\" (",
        "  \"doc_id\", \"ori_sch_id\", \"target_sch_id\", \"reason_text\", \"status\",",
        "  \"target_date\", \"template_id\", \"type\", \"leave_time_length\"",
        ") VALUES (",
        "  #{docId}, #{oriSchId}, #{targetSchId}, #{reasonText}, #{status},",
        "  #{targetDate}, #{templateId}, #{type}, #{leaveTimeLength}",
        ")"
    })
    int insertChangeRequest(@Param("docId") String docId,
                           @Param("oriSchId") String oriSchId,
                           @Param("targetSchId") String targetSchId,
                           @Param("reasonText") String reasonText,
                           @Param("status") String status,
                           @Param("targetDate") String targetDate,
                           @Param("templateId") String templateId,
                           @Param("type") Integer type,
                           @Param("leaveTimeLength") Integer leaveTimeLength);

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
