package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WaitingRecord {
    // 注意：alternate_record 表使用 (patient_id, sch_id) 复合主键，没有 id 列
    private String patientId;
    private String schId;
    private String status;  // 候补中, 已转正, 已过期, 已取消
    private LocalDateTime waitingTime;
    private LocalDateTime promotedTime;
    private LocalDateTime expiredTime;
    private Integer position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
