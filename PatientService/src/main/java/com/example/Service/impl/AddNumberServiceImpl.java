package com.example.Service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.Mapper.AddNumberSourceRecordMapper;
import com.example.Service.AddNumberService;
import com.example.Service.SensitiveOperationService;
import com.example.pojo.dto.AddNumberRequest;
import com.example.pojo.dto.AddNumberStatusDto;
import com.example.pojo.entity.AddNumberSourceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 加号申请服务实现
 */
@Service
public class AddNumberServiceImpl implements AddNumberService {

    @Autowired
    private AddNumberSourceRecordMapper addNumberSourceRecordMapper;

    @Autowired
    private SensitiveOperationService sensitiveOperationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddNumberStatusDto submitAddNumberRequest(AddNumberRequest request) {
        // 0. 黑名单检查
        SensitiveOperationService.BlacklistCheckResult blacklistCheck = 
            sensitiveOperationService.checkBlacklist(request.getPatientId());
        if (blacklistCheck.isInBlacklist()) {
            throw new IllegalArgumentException("您已被加入黑名单，无法进行加号操作。解除时间: " + blacklistCheck.getReleaseTimeFormatted());
        }
        
        // 1. 检查是否已存在该排班的加号申请
        QueryWrapper<AddNumberSourceRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", request.getPatientId())
                    .eq("sch_id", request.getScheduleRecordId());
        
        AddNumberSourceRecord existingRecord = addNumberSourceRecordMapper.selectOne(queryWrapper);
        
        if (existingRecord != null) {
            // 如果已存在且状态为待审核，不允许重复提交
            if ("待审核".equals(existingRecord.getStatus())) {
                throw new RuntimeException("您已提交过该排班的加号申请，请勿重复提交");
            }
            // 如果之前被拒绝或已同意，可以更新申请
            existingRecord.setApplyTime(new Date());
            existingRecord.setStatus("待审核");
            existingRecord.setReasonText(request.getReasonText());
            existingRecord.setReasonPic(request.getReasonPic());
            
            int updated = addNumberSourceRecordMapper.updateById(existingRecord);
            if (updated == 0) {
                throw new RuntimeException("更新加号申请失败");
            }
        } else {
            // 2. 创建新的加号申请记录
            AddNumberSourceRecord record = new AddNumberSourceRecord();
            record.setPatientId(request.getPatientId());
            record.setSchId(request.getScheduleRecordId());
            record.setApplyTime(new Date());
            record.setStatus("待审核");  // 默认状态
            record.setReasonText(request.getReasonText());
            record.setReasonPic(request.getReasonPic());

            // 3. 插入数据库（数据库触发器会自动创建消息通知医生）
            int inserted = addNumberSourceRecordMapper.insert(record);
            if (inserted == 0) {
                throw new RuntimeException("提交加号申请失败");
            }
        }

        // 4. 查询并返回申请详情
        return addNumberSourceRecordMapper.selectAddNumberDetail(
            request.getPatientId(), 
            request.getScheduleRecordId()
        );
    }

    @Override
    public AddNumberStatusDto getAddNumberStatus(String patientId, String scheduleRecordId) {
        AddNumberStatusDto dto = addNumberSourceRecordMapper.selectAddNumberDetail(patientId, scheduleRecordId);
        if (dto == null) {
            throw new RuntimeException("未找到该加号申请记录");
        }
        return dto;
    }

    @Override
    public List<AddNumberStatusDto> getAddNumberHistory(String patientId) {
        return addNumberSourceRecordMapper.selectPatientAddNumberHistory(patientId);
    }
}
