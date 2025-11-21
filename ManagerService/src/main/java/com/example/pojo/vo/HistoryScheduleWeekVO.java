package com.example.pojo.vo;

import com.example.pojo.dto.ScheduleDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HistoryScheduleWeekVO {
    private List<ScheduleDTO> Mon = new ArrayList<>();

    private List<ScheduleDTO> Tue= new ArrayList<>();

    private List<ScheduleDTO> Wed= new ArrayList<>();

    private List<ScheduleDTO> Thu= new ArrayList<>();

    private List<ScheduleDTO> Fri= new ArrayList<>();

    private List<ScheduleDTO> Sat= new ArrayList<>();

    private List<ScheduleDTO> Sun= new ArrayList<>();
}
