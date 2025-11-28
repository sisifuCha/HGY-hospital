// java
package com.example.extra.controller;

import com.example.extra.entity.PatientExtraApply;
import com.example.extra.service.PatientExtraApplyService;
import com.example.conmon.result.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;

@RestController
@RequestMapping("/api/extra-apply")
public class PatientExtraApplyController {

    private final PatientExtraApplyService service;

    public PatientExtraApplyController(@Qualifier("patientExtraApplyServiceImpl") PatientExtraApplyService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Result<PatientExtraApply>> apply(@RequestBody(required = false) PatientExtraApply req) {
        // 基本非空校验
        if (req == null || req.getPatientId() == null || req.getDoctorId() == null
                || req.getDepartmentId() == null || req.getAppointmentDate() == null
                || req.getReason() == null || req.getReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Result.fail(400, "请求参数错误"));
        }
        // 仅允许当天申请
        if (!LocalDate.now().equals(req.getAppointmentDate())) {
            return ResponseEntity.badRequest().body(Result.fail(400, "仅限申请当天加号"));
        }
        // 构造要保存的对象（确保默认字段）
        PatientExtraApply toCreate = PatientExtraApply.builder()
                .patientId(req.getPatientId())
                .departmentId(req.getDepartmentId())
                .doctorId(req.getDoctorId())
                .appointmentDate(req.getAppointmentDate())
                .reason(req.getReason())
                .status("PENDING")
                .locked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PatientExtraApply created = service.apply(toCreate);
        return ResponseEntity.status(201).body(Result.created(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<PatientExtraApply>> getById(@PathVariable Long id) {
        PatientExtraApply r = service.getById(id);
        if (r == null) return ResponseEntity.status(404).body(Result.fail(404, "未找到"));
        return ResponseEntity.ok(Result.success(r));
    }

    @GetMapping
    public ResponseEntity<Result<List<PatientExtraApply>>> listByPatient(@RequestParam Long patientId) {
        List<PatientExtraApply> list = service.listByPatient(patientId);
        return ResponseEntity.ok(Result.success(list));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Result<PatientExtraApply>> approve(@PathVariable Long id, @RequestParam(required = false) Long approverId) {
        PatientExtraApply updated = service.approve(id, approverId);
        return ResponseEntity.ok(Result.success(updated));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Result<PatientExtraApply>> reject(@PathVariable Long id, @RequestParam(required = false) Long approverId, @RequestParam(required = false) String rejectReason) {
        PatientExtraApply updated = service.reject(id, approverId, rejectReason);
        return ResponseEntity.ok(Result.success(updated));
    }
}
