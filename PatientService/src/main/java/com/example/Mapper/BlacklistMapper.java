package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.Blacklist;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 黑名单Mapper
 */
@Mapper
public interface BlacklistMapper extends BaseMapper<Blacklist> {

    /**
     * 根据患者ID查询黑名单记录（通过关联敏感操作表）
     */
    @Select({
        "SELECT b.* FROM blacklist b",
        "JOIN sensitive_operation s ON (b.sen_id1 = s.id OR b.sen_id2 = s.id OR b.sen_id3 = s.id)",
        "WHERE s.patient_id = #{patientId}",
        "LIMIT 1"
    })
    Blacklist findByPatientId(@Param("patientId") String patientId);

    /**
     * 检查患者是否在黑名单中
     */
    @Select({
        "SELECT COUNT(*) FROM blacklist b",
        "JOIN sensitive_operation s ON (b.sen_id1 = s.id OR b.sen_id2 = s.id OR b.sen_id3 = s.id)",
        "WHERE s.patient_id = #{patientId}"
    })
    int isInBlacklist(@Param("patientId") String patientId);

    /**
     * 查询黑名单中所有患者ID及其最晚敏感操作时间（用于清理）
     */
    @Select({
        "SELECT DISTINCT s.patient_id, MAX(s.op_time) as latest_op_time",
        "FROM blacklist b",
        "JOIN sensitive_operation s ON (b.sen_id1 = s.id OR b.sen_id2 = s.id OR b.sen_id3 = s.id)",
        "GROUP BY s.patient_id"
    })
    @Results({
        @Result(property = "patientId", column = "patient_id"),
        @Result(property = "latestOpTime", column = "latest_op_time")
    })
    List<BlacklistPatientInfo> findAllBlacklistPatients();

    /**
     * 删除黑名单记录（通过查询敏感操作的患者ID）
     */
    @Delete({
        "DELETE FROM blacklist WHERE id IN (",
        "  SELECT b.id FROM blacklist b",
        "  JOIN sensitive_operation s ON (b.sen_id1 = s.id OR b.sen_id2 = s.id OR b.sen_id3 = s.id)",
        "  WHERE s.patient_id = #{patientId}",
        ")"
    })
    int deleteByPatientId(@Param("patientId") String patientId);

    /**
     * 生成新的黑名单ID
     */
    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS INTEGER)), 0) + 1 FROM blacklist WHERE id LIKE 'BL%'")
    Integer generateNextId();

    /**
     * 黑名单患者信息
     */
    class BlacklistPatientInfo {
        private String patientId;
        private java.time.LocalDateTime latestOpTime;

        public String getPatientId() {
            return patientId;
        }

        public void setPatientId(String patientId) {
            this.patientId = patientId;
        }

        public java.time.LocalDateTime getLatestOpTime() {
            return latestOpTime;
        }

        public void setLatestOpTime(java.time.LocalDateTime latestOpTime) {
            this.latestOpTime = latestOpTime;
        }
    }
}
