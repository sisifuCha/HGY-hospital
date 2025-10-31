package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("`doctor_schedule`")
public class DoctorSchedule {

    @TableId
    @TableField("schedule_id")
    private String schedule_id;

    @TableField("doctor_id")
    private String doctor_id;

    @TableField("schedule_time_id")
    private String schedule_time_id;

    @TableField("date")
    private LocalDate date;

    @TableField("available_slots")
    private Integer available_slots;
}
