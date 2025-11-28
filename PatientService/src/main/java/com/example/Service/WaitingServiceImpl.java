package com.example.Service;

import com.example.Mapper.RegistrationMapper;
import com.example.Mapper.WaitingMapper;
import com.example.conmon.exception.CreateFailedException;
import com.example.conmon.exception.DuplicateRegistrationException;
import com.example.conmon.exception.SourceFullException;
import com.example.pojo.dto.WaitingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class WaitingServiceImpl implements WaitingService {

    // 每日候补次数限制（可抽取到配置）
    private static final int DAILY_WAITING_LIMIT = 3;

    @Autowired
    private WaitingMapper waitingMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Override
    @Transactional
    public WaitingDto createWaiting(String patientId, String scheduleRecordId) {
        // 参数校验由Controller/DTO负责，额外校验：排班存在
        Integer schExists = waitingMapper.countScheduleRecordById(scheduleRecordId);
        if (schExists == null || schExists == 0) {
            throw new IllegalArgumentException("排班记录不存在");
        }
        // 只允许当号已满时候补：left_source_count <= 0
        Integer left = waitingMapper.getScheduleLeftSource(scheduleRecordId);
        if (left != null && left > 0) {
            throw new SourceFullException();
        }
        // 不允许已有挂号或已有候补
        Integer dupReg = registrationMapper.countActiveRegistrationByKey(patientId, scheduleRecordId);
        if (dupReg != null && dupReg > 0) {
            throw new DuplicateRegistrationException();
        }
        Integer dupWait = waitingMapper.countActiveWaitingByKey(patientId, scheduleRecordId);
        if (dupWait != null && dupWait > 0) {
            throw new DuplicateRegistrationException();
        }
        // 当日次数限制
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        int todayCount = waitingMapper.countWaitingByPatientAndDate(patientId, today);
        if (todayCount >= DAILY_WAITING_LIMIT) {
            throw new IllegalArgumentException("已达到今日候补次数上限");
        }
        // 插入候补记录
        int inserted = waitingMapper.insertWaiting(patientId, scheduleRecordId);
        if (inserted == 0) {
            throw new CreateFailedException();
        }
        // compute position
        int position = waitingMapper.countWaitingBefore(scheduleRecordId, patientId);
        WaitingDto dto = waitingMapper.findWaitingByPatientAndSchedule(patientId, scheduleRecordId);
        if (dto != null) {
            dto.setPosition(position);
            dto.setLimitCount(DAILY_WAITING_LIMIT - todayCount - 1);
        }
        return dto;
    }

    @Override
    public List<WaitingDto> getWaitingListBySchedule(String scheduleRecordId) {
        List<WaitingDto> list = waitingMapper.findWaitingsBySchedule(scheduleRecordId);
        // compute position for each
        for (WaitingDto w : list) {
            int pos = waitingMapper.countWaitingBefore(scheduleRecordId, w.getPatientId());
            w.setPosition(pos);
        }
        return list;
    }

    @Override
    public List<WaitingDto> getWaitingListByPatient(String patientId, String date) {
        List<WaitingDto> list = waitingMapper.findWaitingsByPatient(patientId, date);
        return list;
    }

    @Override
    @Transactional
    public WaitingDto cancelWaiting(String patientId, String waitingId) {
        // 权限与存在校验
        WaitingDto exists = waitingMapper.findWaitingById(waitingId);
        if (exists == null || !patientId.equals(exists.getPatientId())) {
            throw new IllegalArgumentException("无权操作");
        }
        if ("已取消".equals(exists.getStatus()) || "已成功预约".equals(exists.getStatus())) {
            throw new IllegalArgumentException("当前状态不可取消");
        }
        int updated = waitingMapper.updateWaitingStatusToCanceled(waitingId);
        if (updated == 0) {
            throw new CreateFailedException();
        }
        exists.setStatus("已取消");
        return exists;
    }

    @Override
    @Transactional
    public WaitingDto confirmWaiting(String waitingId) {
        // 由系统调用：将排队首位转为正式挂号
        WaitingDto w = waitingMapper.findWaitingById(waitingId);
        if (w == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        if (!"排队中".equals(w.getStatus())) {
            throw new IllegalArgumentException("状态不允许转正");
        }
        // 检查号源
        Integer left = waitingMapper.getScheduleLeftSource(w.getScheduleRecordId());
        if (left == null || left <= 0) {
            throw new IllegalArgumentException("当前号源未释放");
        }
        // 扣减号源
        int dec = registrationMapper.decrementScheduleLeftSource(w.getScheduleRecordId());
        if (dec == 0) {
            throw new IllegalArgumentException("当前号源未释放");
        }
        // 插入正式挂号
        int ins = registrationMapper.insertRegistration(w.getPatientId(), w.getScheduleRecordId(), "已预约");
        if (ins == 0) {
            // 回补
            registrationMapper.incrementScheduleLeftSource(w.getScheduleRecordId());
            throw new CreateFailedException();
        }
        // 更新候补状态
        int upd = waitingMapper.updateWaitingStatusToConfirmed(waitingId, "REG" + System.currentTimeMillis());
        if (upd == 0) {
            throw new CreateFailedException();
        }
        WaitingDto updated = waitingMapper.findWaitingById(waitingId);
        return updated;
    }
}

