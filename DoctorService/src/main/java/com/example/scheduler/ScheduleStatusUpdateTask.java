package com.example.scheduler;

import com.example.mapper.DocScheduleRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 排班状态自动更新定时任务
 * 每天在 12:30 和 18:00 执行，检测并更新已结束的排班状态为2
 */
@Component
public class ScheduleStatusUpdateTask {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleStatusUpdateTask.class);
    
    @Autowired
    private DocScheduleRecordMapper scheduleRecordMapper;

    /**
     * 每天12:30执行，更新上午已结束的排班
     */
    @Scheduled(cron = "0 30 12 * * ?")
    public void updateMorningSchedules() {
        executeStatusUpdate("12:30:00", "上午");
    }

    /**
     * 每天18:00执行，更新下午已结束的排班
     */
    @Scheduled(cron = "0 0 18 * * ?")
    public void updateAfternoonSchedules() {
        executeStatusUpdate("18:00:00", "下午");
    }

    /**
     * 执行排班状态更新
     * @param currentTime 当前检查时间
     * @param period 时段名称（用于日志）
     */
    private void executeStatusUpdate(String currentTime, String period) {
        try {
            LocalDate today = LocalDate.now();
            
            logger.info("==========================================");
            logger.info("开始执行{}排班状态更新任务", period);
            logger.info("检查时间: {}, 日期: {}", currentTime, today);
            
            int updatedCount = scheduleRecordMapper.updateExpiredScheduleStatus(today, currentTime);
            
            if (updatedCount > 0) {
                logger.info("✓ 成功更新 {} 个已结束的排班状态为2", updatedCount);
            } else {
                logger.info("○ 没有需要更新的排班记录");
            }
            
            logger.info("{}排班状态更新任务执行完成", period);
            logger.info("==========================================");
            
        } catch (Exception e) {
            logger.error("✗ 执行排班状态更新任务时发生错误", e);
        }
    }

    /**
     * 手动触发更新（用于测试或手动执行）
     * 可通过调用此方法立即执行状态更新
     */
    public void manualUpdate() {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        logger.info("手动触发排班状态更新，当前时间: {}", currentTime);
        int updatedCount = scheduleRecordMapper.updateExpiredScheduleStatus(today, currentTime);
        logger.info("手动更新完成，更新了 {} 条记录", updatedCount);
    }
}
