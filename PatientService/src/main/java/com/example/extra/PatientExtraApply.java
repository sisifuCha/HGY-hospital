// java
package com.example.extra.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("patient_extra_apply")
public class PatientExtraApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private Long departmentId;
    private Long doctorId;
    private LocalDate appointmentDate;
    private String reason;
    private String status; // PENDING / APPROVED / REJECTED
    private Boolean locked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String rejectReason;
}
