package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WaitingRecord {
    private String id;
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
