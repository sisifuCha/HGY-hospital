package com.example.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 敏感操作和黑名单清理定时任务
 */
@Component
@Slf4j
public class SensitiveOperationCleanupTask {

    @Autowired
    private SensitiveOperationService sensitiveOperationService;

    /**
     * 每天凌晨00:00执行清理任务
     * 清理超过20天的敏感操作记录和黑名单记录
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredRecords() {
        log.info("开始执行敏感操作和黑名单清理定时任务");
        try {
            sensitiveOperationService.cleanupExpiredRecords();
            log.info("敏感操作和黑名单清理定时任务执行完成");
        } catch (Exception e) {
            log.error("敏感操作和黑名单清理定时任务执行失败", e);
        }
    }
}
