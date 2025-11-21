package com.example.pojo.dto;
import lombok.Data;
import java.time.LocalDate;
@Data
public class HistoryScheduleDTO {

    private LocalDate date;

    private String doctor_name;

    private String template_id;
}
