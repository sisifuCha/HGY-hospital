package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 候补规则 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitingRuleDto {
    private String ruleName;        // 规则名称
    private Integer ruleValue;      // 规则值
    private String description;     // 规则描述
}
