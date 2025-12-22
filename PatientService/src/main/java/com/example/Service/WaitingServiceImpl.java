package com.example.Service;

import com.example.Mapper.RegistrationMapper;
import com.example.Mapper.WaitingMapper;
import com.example.conmon.exception.DuplicateRegistrationException;
import com.example.conmon.exception.SourceFullException;
import com.example.pojo.dto.WaitingDto;
import com.example.pojo.entity.WaitingRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WaitingServiceImpl implements WaitingService {

    @Autowired
    private WaitingMapper waitingMapper;

    @Autowired
    private RegistrationMapper registrationMapper;
    
    @Autowired
    private MessageService messageService;

    @Override
    @Transactional
    public WaitingDto createWaiting(String patientId, String scheduleRecordId) {
        // 1. 获取候补规则
        Integer maxWaitingCount = waitingMapper.getRuleValue("MAX_WAITING_COUNT");
        Integer maxPatientWaiting = waitingMapper.getRuleValue("MAX_PATIENT_WAITING");
        Integer stopHoursBefore = waitingMapper.getRuleValue("STOP_HOURS_BEFORE");
        
        if (maxWaitingCount == null) maxWaitingCount = 100;
        if (maxPatientWaiting == null) maxPatientWaiting = 5;
        if (stopHoursBefore == null) stopHoursBefore = 3;
        
        // 2. 校验排班是否存在
        Integer schExists = waitingMapper.countScheduleRecordById(scheduleRecordId);
        if (schExists == null || schExists == 0) {
            throw new IllegalArgumentException("排班记录不存在");
        }

        // 3. 校验号源是否已满 (只有满号才能候补)
        Integer left = waitingMapper.getScheduleLeftSource(scheduleRecordId);
        if (left != null && left > 0) {
            throw new SourceFullException();
        }

        // 4. 校验是否已有有效挂号
        Integer dupReg = registrationMapper.countActiveRegistrationByKey(patientId, scheduleRecordId);
        if (dupReg != null && dupReg > 0) {
            throw new DuplicateRegistrationException();
        }

        // 5. 检查是否超过排班候补人数上限
        Integer currentWaitingCount = waitingMapper.getWaitingCountBySchedule(scheduleRecordId);
        if (currentWaitingCount != null && currentWaitingCount >= maxWaitingCount) {
            throw new IllegalArgumentException("该排班候补人数已达上限");
        }
        
        // 6. 检查患者候补总数是否超限
        Integer patientWaitingCount = waitingMapper.getPatientWaitingCount(patientId);
        if (patientWaitingCount != null && patientWaitingCount >= maxPatientWaiting) {
            throw new IllegalArgumentException("您的候补数量已达上限");
        }
        
        // 7. 检查是否重复候补
        Integer duplicate = waitingMapper.checkDuplicateWaiting(patientId, scheduleRecordId);
        if (duplicate != null && duplicate > 0) {
            throw new DuplicateRegistrationException();
        }
        
        // 8. 检查是否已截止候补（就诊前3小时停止候补）
        LocalDateTime scheduleStartTime = waitingMapper.getScheduleStartTime(scheduleRecordId);
        LocalDateTime stopWaitingTime = scheduleStartTime.minusHours(stopHoursBefore);
        if (LocalDateTime.now().isAfter(stopWaitingTime)) {
            throw new IllegalArgumentException("该排班已停止候补");
        }
        
        // 9. 插入候补记录到数据库
        WaitingRecord record = new WaitingRecord();
        record.setPatientId(patientId);
        record.setSchId(scheduleRecordId);
        record.setStatus("候补中");
        record.setWaitingTime(LocalDateTime.now());

        int inserted = waitingMapper.insertWaitingRecord(record);
        if (inserted == 0) {
            throw new RuntimeException("候补记录插入失败");
        }

        // 10. 计算候补位置
        Integer position = waitingMapper.getWaitingPosition(patientId, scheduleRecordId);

        // 11. 构造返回对象（使用复合主键组合作为 waitingId）
        WaitingDto dto = new WaitingDto();
        dto.setWaitingId(patientId + "_" + scheduleRecordId);
        dto.setPatientId(patientId);
        dto.setScheduleRecordId(scheduleRecordId);
        dto.setApplyTime(record.getWaitingTime().toString());
        dto.setStatus("候补中");
        dto.setPosition(position != null ? position : 1);
        dto.setLimitCount(maxPatientWaiting - (patientWaitingCount != null ? patientWaitingCount : 0) - 1);
        
        // 12. 发送候补成功消息
        try {
            int pos = position != null ? position : 1;
            messageService.sendWaitingPositionUpdateMessage(patientId, scheduleRecordId, "医生", pos);
        } catch (Exception e) {
            log.warn("Failed to send waiting position message: {}", e.getMessage());
        }
        
        return dto;
    }

    @Override
    public List<WaitingDto> getWaitingListBySchedule(String scheduleRecordId) {
        List<WaitingRecord> records = waitingMapper.getWaitingRecordsBySchedule(scheduleRecordId, "候补中");
        List<WaitingDto> result = new ArrayList<>();
        
        for (int i = 0; i < records.size(); i++) {
            WaitingRecord record = records.get(i);
            WaitingDto dto = new WaitingDto();
            dto.setWaitingId(record.getPatientId() + "_" + record.getSchId());
            dto.setPatientId(record.getPatientId());
            dto.setScheduleRecordId(record.getSchId());
            dto.setApplyTime(record.getWaitingTime().toString());
            dto.setStatus(record.getStatus());
            dto.setPosition(i + 1);  // 按 waiting_time 排序，索引+1即为位置
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public List<WaitingDto> getWaitingListByPatient(String patientId, String date) {
        List<WaitingRecord> records = waitingMapper.getWaitingRecordsByPatient(patientId);
        List<WaitingDto> result = new ArrayList<>();
        
        for (WaitingRecord record : records) {
            WaitingDto dto = new WaitingDto();
            dto.setWaitingId(record.getPatientId() + "_" + record.getSchId());
            dto.setPatientId(record.getPatientId());
            dto.setScheduleRecordId(record.getSchId());
            dto.setApplyTime(record.getWaitingTime().toString());
            dto.setStatus(record.getStatus());
            
            // 如果是候补中，计算当前位置
            if ("候补中".equals(record.getStatus())) {
                Integer position = waitingMapper.getWaitingPosition(patientId, record.getSchId());
                dto.setPosition(position != null ? position : 0);
            }
            
            result.add(dto);
        }
        
        return result;
    }

    @Override
    @Transactional
    public WaitingDto cancelWaiting(String patientId, String scheduleRecordId) {
        // 1. 查询候补记录
        WaitingRecord record = waitingMapper.getWaitingRecordByKey(patientId, scheduleRecordId);
        if (record == null) {
            throw new IllegalArgumentException("未找到候补记录");
        }
        
        if (!"候补中".equals(record.getStatus())) {
            throw new IllegalArgumentException("该候补记录状态不允许取消");
        }
        
        // 2. 更新状态为已取消
        int updated = waitingMapper.cancelWaitingRecord(patientId, scheduleRecordId);
        if (updated == 0) {
            throw new RuntimeException("取消候补失败");
        }
        
        // 3. 构造返回对象
        WaitingDto dto = new WaitingDto();
        dto.setWaitingId(record.getPatientId() + "_" + record.getSchId());
        dto.setPatientId(patientId);
        dto.setScheduleRecordId(scheduleRecordId);
        dto.setApplyTime(record.getWaitingTime().toString());
        dto.setStatus("已取消");
        
        return dto;
    }

    @Override
    public WaitingDto confirmWaiting(String waitingId) {
        // 预留方法，当前不实现
        return null;
    }
}

