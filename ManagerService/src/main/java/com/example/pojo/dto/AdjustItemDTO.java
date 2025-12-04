package com.example.pojo.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AdjustItemDTO {
    String id;
    String docId;
    String docName;
    String oriScheId;
    String targetScheId;
    String reason;
    String status;
    LocalDate targetDate;
    Integer type;
}
