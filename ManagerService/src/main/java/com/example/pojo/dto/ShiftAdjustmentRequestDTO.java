package com.example.pojo.dto;

import lombok.Data;

/**
 * 班次变更申请请求DTO
 * 用于接收医生班次变更申请的参数
 */
@Data
public class ShiftAdjustmentRequestDTO {
    /**
     * 原班次ID
     */
    private String id;

    private String doc_id;
    
    /**
     * 变更类型：0代表调班，1代表请假
     */
    private Integer changeType;
    
    /**
     * 目标日期
     */
    private String targetDate;
    
    /**
     * 目标时段：上午是TIME0001，下午是TIME0002
     */
    private String targetTime;
    
    /**
     * 调班或请假的原因
     */
    private String reason;
}