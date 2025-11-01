package com.example.pojo.vo;

import com.example.pojo.entity.DoctorSchedule;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//返回给前端的视图数据
@Data
public class ScheduleWeekVO {

    private List<DoctorSchedule> Mon = new ArrayList<>();

    private List<DoctorSchedule> Tue= new ArrayList<>();

    private List<DoctorSchedule> Wed= new ArrayList<>();

    private List<DoctorSchedule> Thu= new ArrayList<>();

    private List<DoctorSchedule> Fri= new ArrayList<>();

    private List<DoctorSchedule> Sat= new ArrayList<>();

    private List<DoctorSchedule> Sun= new ArrayList<>();

}
