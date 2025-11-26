// java
package com.example.extra.service;

import com.example.extra.entity.PatientExtraApply;
import java.util.List;

public interface PatientExtraApplyService {
    PatientExtraApply apply(PatientExtraApply apply);
    PatientExtraApply getById(Long id);
    List<PatientExtraApply> listByPatient(Long patientId);
    PatientExtraApply approve(Long id, Long approverId);
    PatientExtraApply reject(Long id, Long approverId, String rejectReason);
}
