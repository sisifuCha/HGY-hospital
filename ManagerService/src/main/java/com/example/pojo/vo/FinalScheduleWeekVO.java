package com.example.pojo.vo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FinalScheduleWeekVO {
    private List<FinalScheduleVO> Mon = new ArrayList<>();

    private List<FinalScheduleVO> Tue= new ArrayList<>();

    private List<FinalScheduleVO> Wed= new ArrayList<>();

    private List<FinalScheduleVO> Thu= new ArrayList<>();

    private List<FinalScheduleVO> Fri= new ArrayList<>();

    private List<FinalScheduleVO> Sat= new ArrayList<>();

    private List<FinalScheduleVO> Sun= new ArrayList<>();
}
