package com.example.pojo.dto;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class NextWeekScheduleDTO {

    private List<ScheduleDTO> mon = new ArrayList<>();

    private List<ScheduleDTO> tue= new ArrayList<>();

    private List<ScheduleDTO> wed= new ArrayList<>();

    private List<ScheduleDTO> thu= new ArrayList<>();

    private List<ScheduleDTO> fri= new ArrayList<>();

    private List<ScheduleDTO> sat= new ArrayList<>();

    private List<ScheduleDTO> sun= new ArrayList<>();
}
