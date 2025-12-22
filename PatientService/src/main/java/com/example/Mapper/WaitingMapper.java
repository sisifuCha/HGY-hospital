package com.example.Mapper;

import com.example.pojo.dto.WaitingDto;
import com.example.pojo.entity.WaitingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WaitingMapper {
    int insertWaiting(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId);
    WaitingDto findWaitingByPatientAndSchedule(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId);
    WaitingDto findWaitingById(@Param("waitingId") String waitingId);
    int countActiveWaitingByKey(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId);
    int countWaitingByPatientAndDate(@Param("patientId") String patientId, @Param("date") String date);
    int countWaitingBefore(@Param("scheduleRecordId") String scheduleRecordId, @Param("patientId") String patientId);
    List<WaitingDto> findWaitingsBySchedule(@Param("scheduleRecordId") String scheduleRecordId);
    List<WaitingDto> findWaitingsByPatient(@Param("patientId") String patientId, @Param("date") String date);
    int updateWaitingStatusToCanceled(@Param("waitingId") String waitingId);
    int updateWaitingStatusToConfirmed(@Param("waitingId") String waitingId, @Param("registrationId") String registrationId);
    
    // 新增规则相关方法
    /**
     * 获取候补规则值
     */
    Integer getRuleValue(@Param("ruleName") String ruleName);
    
    /**
     * 获取排班当前候补人数（从数据库）
     */
    Integer getWaitingCountBySchedule(@Param("schId") String schId);
    
    /**
     * 获取患者当前候补总数（从数据库）
     */
    Integer getPatientWaitingCount(@Param("patientId") String patientId);
    
    /**
     * 检查患者是否已在该排班候补
     */
    Integer checkDuplicateWaiting(@Param("patientId") String patientId, @Param("schId") String schId);
    
    /**
     * 插入候补记录到 waiting_record 表
     */
    int insertWaitingRecord(@Param("record") WaitingRecord record);
    
    /**
     * 更新候补记录状态
     */
    int updateWaitingRecordStatus(@Param("patientId") String patientId, @Param("schId") String schId, @Param("status") String status, @Param("updateTime") LocalDateTime updateTime);
    
    /**
     * 获取排班的所有候补记录
     */
    List<WaitingRecord> getWaitingRecordsBySchedule(@Param("schId") String schId, @Param("status") String status);
    
    /**
     * 获取患者的所有候补记录
     */
    List<WaitingRecord> getWaitingRecordsByPatient(@Param("patientId") String patientId);
    
    /**
     * 批量更新过期候补记录
     */
    int batchExpireWaitingRecords(@Param("schId") String schId, @Param("expiredTime") LocalDateTime expiredTime);
    
    /**
     * 取消患者的候补记录
     */
    int cancelWaitingRecord(@Param("patientId") String patientId, @Param("schId") String schId);
    
    /**
     * 获取排班开始时间
     */
    LocalDateTime getScheduleStartTime(@Param("schId") String schId);
    
    /**
     * 获取下一个候补患者（按 waiting_time 排序）
     */
    WaitingRecord getNextWaitingPatient(@Param("schId") String schId);
    
    /**
     * 标记候补记录为已转正
     */
    int promoteWaitingRecord(@Param("patientId") String patientId, @Param("schId") String schId, @Param("promotedTime") LocalDateTime promotedTime);
    
    /**
     * 获取患者在某个排班的候补位置
     */
    Integer getWaitingPosition(@Param("patientId") String patientId, @Param("schId") String schId);
    
    /**
     * 根据主键查询候补记录
     */
    WaitingRecord getWaitingRecordByKey(@Param("patientId") String patientId, @Param("schId") String schId);

    // helpers
    Integer countScheduleRecordById(@Param("scheduleRecordId") String scheduleRecordId);
    Integer getScheduleLeftSource(@Param("scheduleRecordId") String scheduleRecordId);
}

