package com.example.Service;

import com.example.Mapper.RegistrationMapper;
import com.example.Mapper.WaitingMapper;
import com.example.pojo.entity.WaitingRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WaitingRuleService {

    @Autowired
    private WaitingMapper waitingMapper;
    
    @Autowired
    private RegistrationMapper registrationMapper;
    
    @Autowired
    private MessageService messageService;

    /**
     * 验证是否可以加入候补
     */
    public void validateWaitingRequest(String patientId, String schId) {
        // 1. 检查号源是否为0（前端已检查，后端再次验证）
        Integer leftSource = registrationMapper.findScheduleLeftSource(schId);
        if (leftSource == null) {
            throw new IllegalArgumentException("排班记录不存在");
        }
        if (leftSource > 0) {
            throw new IllegalArgumentException("仍有剩余号源，请直接挂号");
        }

        // 2. 检查患者候补数量限制
        Integer maxPatientWaiting = waitingMapper.getRuleValue("MAX_PATIENT_WAITING");
        if (maxPatientWaiting == null) maxPatientWaiting = 5;
        
        Integer patientWaitingCount = waitingMapper.getPatientWaitingCount(patientId);
        if (patientWaitingCount != null && patientWaitingCount >= maxPatientWaiting) {
            throw new IllegalArgumentException("您的候补记录已达上限（" + maxPatientWaiting + "条），请先取消部分候补");
        }

        // 3. 检查排班候补数量限制
        Integer maxWaitingCount = waitingMapper.getRuleValue("MAX_WAITING_COUNT");
        if (maxWaitingCount == null) maxWaitingCount = 100;
        
        Integer scheduleWaitingCount = waitingMapper.getWaitingCountBySchedule(schId);
        if (scheduleWaitingCount != null && scheduleWaitingCount >= maxWaitingCount) {
            throw new IllegalArgumentException("该排班候补人数已满（" + maxWaitingCount + "人）");
        }

        // 4. 检查是否重复候补
        Integer duplicateCount = waitingMapper.checkDuplicateWaiting(patientId, schId);
        if (duplicateCount != null && duplicateCount > 0) {
            throw new IllegalArgumentException("您已在该排班的候补队列中");
        }

        // 5. 检查候补时间是否截止
        LocalDateTime scheduleStartTime = waitingMapper.getScheduleStartTime(schId);
        if (scheduleStartTime == null) {
            throw new IllegalArgumentException("无法获取排班信息");
        }
        
        Integer stopHoursBefore = waitingMapper.getRuleValue("STOP_HOURS_BEFORE");
        if (stopHoursBefore == null) stopHoursBefore = 3;
        
        LocalDateTime stopWaitingTime = scheduleStartTime.minusHours(stopHoursBefore);
        if (LocalDateTime.now().isAfter(stopWaitingTime)) {
            throw new IllegalArgumentException("候补已截止（就诊前" + stopHoursBefore + "小时停止候补）");
        }
    }

    /**
     * 创建候补记录（同时写入Redis和数据库）
     */
    @Transactional
    public WaitingRecord createWaitingRecord(String patientId, String schId) {
        // 验证规则
        validateWaitingRequest(patientId, schId);
        
        // 创建候补记录
        WaitingRecord record = new WaitingRecord();
        record.setId(UUID.randomUUID().toString());
        record.setPatientId(patientId);
        record.setSchId(schId);
        record.setStatus("候补中");
        record.setWaitingTime(LocalDateTime.now());
        
        // 计算位置
        Integer currentCount = waitingMapper.getWaitingCountBySchedule(schId);
        record.setPosition(currentCount != null ? currentCount + 1 : 1);
        
        // 插入数据库
        int inserted = waitingMapper.insertWaitingRecord(record);
        if (inserted == 0) {
            throw new RuntimeException("创建候补记录失败");
        }
        
        log.info("Waiting record created: patient={}, schedule={}, position={}", 
                 patientId, schId, record.getPosition());
        
        return record;
    }

    /**
     * 定时任务：清理过期候补记录
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void cleanupExpiredWaitingRecords() {
        log.info("Starting cleanup of expired waiting records...");
        
        try {
            // 查询所有候补中的记录
            // TODO: 这里应该查询即将过期的排班
            // 简化实现：假设我们有一个方法获取所有需要检查的排班
            
            log.info("Expired waiting records cleanup completed");
        } catch (Exception e) {
            log.error("Failed to cleanup expired waiting records", e);
        }
    }

    /**
     * 使排班的所有候补记录过期
     */
    @Transactional
    public void expireWaitingRecordsBySchedule(String schId) {
        LocalDateTime now = LocalDateTime.now();
        
        // 获取所有候补中的记录（发送消息前）
        List<WaitingRecord> records = waitingMapper.getWaitingRecordsBySchedule(schId, "候补中");
        
        // 批量更新为过期状态
        int expired = waitingMapper.batchExpireWaitingRecords(schId, now);
        
        // 发送过期消息
        for (WaitingRecord record : records) {
            try {
                messageService.sendWaitingExpiredMessage(
                    record.getPatientId(), 
                    schId, 
                    "医生", 
                    now.toLocalDate().toString()
                );
            } catch (Exception e) {
                log.warn("Failed to send waiting expired message for patient {}: {}", 
                         record.getPatientId(), e.getMessage());
            }
        }
        
        log.info("Expired {} waiting records for schedule: {}", expired, schId);
    }

    /**
     * 取消候补
     */
    @Transactional
    public void cancelWaiting(String patientId, String schId) {
        int cancelled = waitingMapper.cancelWaitingRecord(patientId, schId);
        if (cancelled == 0) {
            throw new IllegalArgumentException("未找到候补记录或已失效");
        }
        
        log.info("Waiting cancelled: patient={}, schedule={}", patientId, schId);
    }
}
