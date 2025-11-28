package com.example.pojo.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FinalScheduleVO {
    private String doc_name;
    private String left_source_count;
    private String depart;
    private String title;
    private String template_id;
    private LocalDate date;
}
