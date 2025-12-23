package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 挂号费用预览 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeePreviewDto {

    // 排班信息
    private String scheduleRecordId;        // 排班记录ID
    private String doctorId;                // 医生ID
    private String doctorName;              // 医生姓名
    private String doctorTitle;             // 医生职称
    private String departmentName;          // 科室名称
    private String scheduleDate;            // 就诊日期
    private String timePeriod;              // 时段（如"上午 08:00-12:00"）

    // 费用明细
    private BigDecimal registrationFee;     // 挂号费原价
    private String reimburseType;           // 报销类型（职工医保/居民医保/无医保）
    private BigDecimal reimbursePercent;    // 报销比例（80表示80%）
    private BigDecimal reimbursedAmount;    // 报销金额（原价 × 报销比例）
    private BigDecimal actualPayAmount;     // 实际需支付金额（原价 - 报销金额）

    // 医保账户信息
    private BigDecimal medicalInsuranceBalance;  // 医保账户余额
    private Boolean canAfford;              // 是否有足够余额支付
}
