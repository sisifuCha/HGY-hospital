package com.example.pojo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TimeDTO {
    LocalDate date;
    String template_id;
}
