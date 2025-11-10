package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.Conmon.CommonData;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("`doc_schedule_record`")
public class DoctorSchedule {

    @TableId
    @TableField("id")
    private String schedule_id;

    @TableField("doc_id")
    private String doctor_id;

    @TableField("template_id")
    private String schedule_time_id;

    @TableField("schedule_date")
    private LocalDate date;

    @TableField("left_source_count")
    private Integer available_slots = CommonData.DEFAULT_LEFT_SOURCE_COUNT;
}
