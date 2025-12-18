package com.example.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class PaymentDto {
    private String paymentId;
    private String patientId;
    private String scheduleRecordId;
    private String doctorId;
    private String doctorName;
    private String departmentName;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime payTime;
    
    private String payStatus;
    private BigDecimal oriAmount;
    private BigDecimal askPayAmount;
    private BigDecimal reimbursePercent;
    private String reimburseType;
}
