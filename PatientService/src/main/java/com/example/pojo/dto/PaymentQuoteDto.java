package com.example.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentQuoteDto {
    private String patientId;
    private String scheduleRecordId;

    /** 原始挂号费 */
    private BigDecimal oriAmount;

    /** 报销类型名称（例如：职工医保/自费） */
    private String reimburseType;

    /** 报销比例（百分比，0-100） */
    private BigDecimal reimbursePercent;

    /** 报销金额 = oriAmount - askPayAmount */
    private BigDecimal reimburseAmount;

    /** 实际需支付金额（优惠后） */
    private BigDecimal askPayAmount;

    /** 医保余额（可能为空：未绑定医保账户） */
    private BigDecimal medicalInsuranceOverage;

    /** 医保余额是否足够覆盖 askPayAmount */
    private Boolean insuranceEnough;
}

