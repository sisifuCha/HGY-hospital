package com.example.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RefundRate {
    private Integer id;
    private BigDecimal hoursBefore;
    private BigDecimal refundRate;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
