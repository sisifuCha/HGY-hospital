package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WaitingRule {
    private Integer id;
    private String ruleName;
    private Integer ruleValue;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}