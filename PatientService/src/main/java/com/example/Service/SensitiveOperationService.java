package com.example.Service;

import com.example.Mapper.BlacklistMapper;
import com.example.Mapper.RegistrationMapper;
import com.example.Mapper.SensitiveOperationMapper;
import com.example.pojo.entity.Blacklist;
import com.example.pojo.entity.SensitiveOperation;
import com.example.pojo.vo.RegistrationVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 敏感操作和黑名单管理服务
 */
@Service
@Slf4j
public class SensitiveOperationService {

    @Autowired
    private SensitiveOperationMapper sensitiveOperationMapper;

    @Autowired
    private BlacklistMapper blacklistMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    private static final int SENSITIVE_OP_THRESHOLD = 3; // 敏感操作次数阈值
    private static final int COOLDOWN_DAYS = 20; // 冷却天数

    /**
     * 检查是否为敏感操作并记录
     * 规则：在就诊开始前3小时到就诊结束之间退号视为敏感操作
     * 
     * @param patientId 患者ID
     * @param scheduleRecordId 排班记录ID
     * @return 是否为敏感操作
     */
    @Transactional
    public boolean checkAndRecordSensitiveOperation(String patientId, String scheduleRecordId) {
        // 1. 获取挂号记录，包含就诊时间信息
        RegistrationVo registration = registrationMapper.findRegistrationByPatientAndSchedule(patientId, scheduleRecordId);
        if (registration == null) {
            log.warn("未找到挂号记录: patientId={}, scheduleRecordId={}", patientId, scheduleRecordId);
            return false;
        }

        // 2. 构建就诊开始和结束时间
        LocalDateTime appointmentStart = registration.getRegistrationDate().atTime(
            LocalTime.parse(registration.getTimePeriodName().contains("上午") ? "08:00:00" : "13:30:00")
        );
        
        // 假设每个时段持续4小时
        LocalDateTime appointmentEnd = appointmentStart.plusHours(4);
        
        // 3. 计算敏感时间窗口：就诊开始前3小时到就诊结束
        LocalDateTime sensitiveStart = appointmentStart.minusHours(3);
        LocalDateTime sensitiveEnd = appointmentEnd;
        
        LocalDateTime now = LocalDateTime.now();
        
        // 4. 判断当前时间是否在敏感时间窗口内
        boolean isSensitive = now.isAfter(sensitiveStart) && now.isBefore(sensitiveEnd);
        
        if (isSensitive) {
            log.warn("检测到敏感操作: 患者{}在敏感时间窗口内退号 [{} ~ {}]", 
                     patientId, sensitiveStart, sensitiveEnd);
            recordSensitiveOperation(patientId, "就诊前3小时内退号", 
                String.format("原定就诊时间: %s, 退号时间: %s", appointmentStart, now));
        }
        
        return isSensitive;
    }

    /**
     * 记录敏感操作
     * 
     * @param patientId 患者ID
     * @param opType 操作类型
     * @param remark 备注
     */
    @Transactional
    public void recordSensitiveOperation(String patientId, String opType, String remark) {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 生成敏感操作ID
        Integer nextNum = sensitiveOperationMapper.generateNextId();
        String senId = String.format("SEN%04d", nextNum != null ? nextNum : 1);
        
        // 2. 创建敏感操作记录
        SensitiveOperation operation = new SensitiveOperation();
        operation.setId(senId);
        operation.setPatientId(patientId);
        operation.setSensitiveOpType(opType);
        operation.setOpTime(now);
        operation.setRemark(remark);
        
        sensitiveOperationMapper.insert(operation);
        log.info("记录敏感操作: ID={}, 患者={}, 类型={}", senId, patientId, opType);
        
        // 3. 更新该患者所有敏感操作的时间（统一更新倒计时）
        int updated = sensitiveOperationMapper.updateAllOpTimeByPatientId(patientId, now);
        log.info("更新患者{}的{}条敏感操作记录时间", patientId, updated);
        
        // 4. 检查敏感操作次数，判断是否需要加入黑名单
        int count = sensitiveOperationMapper.countByPatientId(patientId);
        log.info("患者{}当前敏感操作次数: {}", patientId, count);
        
        if (count >= SENSITIVE_OP_THRESHOLD) {
            addToBlacklist(patientId, now);
        }
    }

    /**
     * 将患者加入黑名单
     * 
     * @param patientId 患者ID
     * @param latestOpTime 最新敏感操作时间
     */
    @Transactional
    public void addToBlacklist(String patientId, LocalDateTime latestOpTime) {
        // 1. 检查是否已在黑名单中
        if (blacklistMapper.isInBlacklist(patientId) > 0) {
            log.info("患者{}已在黑名单中", patientId);
            return;
        }
        
        // 2. 获取最近3条敏感操作ID
        List<String> senIds = sensitiveOperationMapper.findTop3IdsByPatientId(patientId);
        if (senIds.size() < 3) {
            log.warn("患者{}敏感操作记录不足3条，无法加入黑名单", patientId);
            return;
        }
        
        // 3. 生成黑名单ID
        Integer nextNum = blacklistMapper.generateNextId();
        String blacklistId = String.format("BL%04d", nextNum != null ? nextNum : 1);
        
        // 4. 创建黑名单记录
        Blacklist blacklist = new Blacklist();
        blacklist.setId(blacklistId);
        blacklist.setSenId1(senIds.get(0));
        blacklist.setSenId2(senIds.get(1));
        blacklist.setSenId3(senIds.get(2));
        blacklist.setCount((short) senIds.size());
        
        blacklistMapper.insert(blacklist);
        log.warn("患者{}已加入黑名单: ID={}, 敏感操作次数={}", patientId, blacklistId, senIds.size());
    }

    /**
     * 检查患者是否在黑名单中
     * 
     * @param patientId 患者ID
     * @return 黑名单信息，如果不在黑名单中则返回null
     */
    public BlacklistCheckResult checkBlacklist(String patientId) {
        if (blacklistMapper.isInBlacklist(patientId) > 0) {
            // 获取最新的敏感操作时间
            SensitiveOperation latestOp = sensitiveOperationMapper.findLatestByPatientId(patientId);
            if (latestOp != null) {
                LocalDateTime releaseTime = latestOp.getOpTime().plusDays(COOLDOWN_DAYS);
                return new BlacklistCheckResult(true, releaseTime);
            }
            return new BlacklistCheckResult(true, null);
        }
        return new BlacklistCheckResult(false, null);
    }

    /**
     * 定时清理过期的敏感操作和黑名单记录
     * 每天00:00执行
     */
    @Transactional
    public void cleanupExpiredRecords() {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(COOLDOWN_DAYS);
        log.info("开始清理过期的敏感操作和黑名单记录，截止时间: {}", expireTime);
        
        // 1. 查找所有过期的患者ID
        List<String> expiredPatientIds = sensitiveOperationMapper.findExpiredPatientIds(expireTime);
        log.info("找到{}个患者的记录已过期", expiredPatientIds.size());
        
        int cleanedSensitive = 0;
        int cleanedBlacklist = 0;
        
        for (String patientId : expiredPatientIds) {
            // 检查该患者最新的敏感操作是否已过期
            SensitiveOperation latestOp = sensitiveOperationMapper.findLatestByPatientId(patientId);
            if (latestOp != null && latestOp.getOpTime().isBefore(expireTime)) {
                // 2. 删除黑名单记录
                int deletedBlacklist = blacklistMapper.deleteByPatientId(patientId);
                if (deletedBlacklist > 0) {
                    cleanedBlacklist++;
                    log.info("从黑名单中移除患者: {}", patientId);
                }
                
                // 3. 删除敏感操作记录
                int deletedSensitive = sensitiveOperationMapper.deleteByPatientId(patientId);
                if (deletedSensitive > 0) {
                    cleanedSensitive++;
                    log.info("删除患者{}的{}条敏感操作记录", patientId, deletedSensitive);
                }
            }
        }
        
        log.info("清理完成: 清理{}位患者的敏感操作记录，{}位患者从黑名单中移除", cleanedSensitive, cleanedBlacklist);
    }

    /**
     * 黑名单检查结果
     */
    public static class BlacklistCheckResult {
        private boolean inBlacklist;
        private LocalDateTime releaseTime;

        public BlacklistCheckResult(boolean inBlacklist, LocalDateTime releaseTime) {
            this.inBlacklist = inBlacklist;
            this.releaseTime = releaseTime;
        }

        public boolean isInBlacklist() {
            return inBlacklist;
        }

        public LocalDateTime getReleaseTime() {
            return releaseTime;
        }

        public String getReleaseTimeFormatted() {
            if (releaseTime == null) {
                return "未知";
            }
            return releaseTime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
        }
    }
}
