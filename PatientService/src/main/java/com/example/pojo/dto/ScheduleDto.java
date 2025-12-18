package com.example.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class ScheduleDto {
    private String scheduleId; // 排班记录ID (sch_id)
    private String startTime;
    private String endTime;
    private String timePeriodName; // e.g., "上午"
    private Integer leftSourceCount; // 剩余号源
    private BigDecimal registrationFee;
    
    // 新增候补相关字段
    private Boolean canRegister;      // 是否可直接挂号
    private Boolean canWaiting;       // 是否可候补
    private Integer waitingCount;     // 当前候补人数
    private Boolean waitingClosed;    // 候补是否已截止
}

