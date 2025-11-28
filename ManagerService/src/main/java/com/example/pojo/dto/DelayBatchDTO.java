package com.example.pojo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class DelayBatchDTO {
    List<String> doc_ids = new ArrayList<String>();
    TimeDTO start_time;
    TimeDTO end_time;
    String reason;
    Integer delay_days;
}
