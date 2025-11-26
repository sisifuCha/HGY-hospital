// java
package com.example.extra.controller;

import com.example.extra.entity.PatientExtraApply;
import com.example.extra.service.PatientExtraApplyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/extra-apply")
public class PatientExtraApplyController {

    private final PatientExtraApplyService service;

    public PatientExtraApplyController(PatientExtraApplyService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PatientExtraApply> apply(@RequestBody PatientExtraApply req) {
        PatientExtraApply created = service.apply(req);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientExtraApply> getById(@PathVariable Long id) {
        PatientExtraApply r = service.getById(id);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
    }

    @GetMapping
    public ResponseEntity<List<PatientExtraApply>> listByPatient(@RequestParam Long patientId) {
        return ResponseEntity.ok(service.listByPatient(patientId));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<PatientExtraApply> approve(@PathVariable Long id, @RequestParam(required = false) Long approverId) {
        PatientExtraApply updated = service.approve(id, approverId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<PatientExtraApply> reject(@PathVariable Long id, @RequestParam(required = false) Long approverId, @RequestParam(required = false) String rejectReason) {
        PatientExtraApply updated = service.reject(id, approverId, rejectReason);
        return ResponseEntity.ok(updated);
    }
}
