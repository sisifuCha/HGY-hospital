package com.example.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MedicalInsurance {
    private String id;
    private BigDecimal overage;
}
