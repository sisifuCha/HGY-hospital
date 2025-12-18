package com.example.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReimburseType {
    private String id;
    private String type;
    private BigDecimal percent;
}
