// java
package com.example.extra.service.impl;

import com.example.extra.entity.PatientExtraApply;
import com.example.extra.mapper.PatientExtraApplyMapper;
import com.example.extra.service.PatientExtraApplyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Primary;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Primary
@Service
public class PatientExtraApplyServiceImpl implements PatientExtraApplyService {

    private final PatientExtraApplyMapper mapper;

    public PatientExtraApplyServiceImpl(PatientExtraApplyMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public PatientExtraApply apply(PatientExtraApply apply) {
        // 仅允许当天申请
        LocalDate today = LocalDate.now();
        if (!Objects.equals(apply.getAppointmentDate(), today)) {
            throw new IllegalArgumentException("仅限申请当天加号");
        }
        apply.setStatus("PENDING");
        apply.setLocked(false);
        apply.setCreatedAt(LocalDateTime.now());
        apply.setUpdatedAt(LocalDateTime.now());
        mapper.insert(apply);
        return apply;
    }

    @Override
    public PatientExtraApply getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<PatientExtraApply> listByPatient(Long patientId) {
        return mapper.selectList(new QueryWrapper<PatientExtraApply>().eq("patient_id", patientId).orderByDesc("created_at"));
    }

    @Override
    @Transactional
    public PatientExtraApply approve(Long id, Long approverId) {
        PatientExtraApply record = mapper.selectById(id);
        if (record == null) throw new IllegalStateException("申请不存在");
        if (!"PENDING".equals(record.getStatus())) throw new IllegalStateException("申请已处理");
        record.setStatus("APPROVED");
        record.setLocked(true);
        record.setUpdatedAt(LocalDateTime.now());
        mapper.updateById(record);
        // TODO: 触发缴费流程或消息（项目内集成）
        return record;
    }

    @Override
    @Transactional
    public PatientExtraApply reject(Long id, Long approverId, String rejectReason) {
        PatientExtraApply record = mapper.selectById(id);
        if (record == null) throw new IllegalStateException("申请不存在");
        if (!"PENDING".equals(record.getStatus())) throw new IllegalStateException("申请已处理");
        record.setStatus("REJECTED");
        record.setLocked(false);
        record.setRejectReason(rejectReason);
        record.setUpdatedAt(LocalDateTime.now());
        mapper.updateById(record);
        return record;
    }
}
