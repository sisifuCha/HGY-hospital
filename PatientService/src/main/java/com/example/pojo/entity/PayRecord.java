package com.example.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class PayRecord {
    private String id;
    private ZonedDateTime payTime;
    private String payStatus;
    private BigDecimal oriAmount;
    private BigDecimal askPayAmount;
    private String patientId;
    private String docId;
    private String schId;
}
