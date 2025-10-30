package com.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("add_number_source_record")
public class AddNumberSourceRecordEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "patient_ID")
    private String patientId;

    @TableField("sch_ID")
    private String schId;

    @TableField("apply_time")
    private Date applyTime;

    @TableField("status")
    private String status;

    @TableField("reason_text")
    private String reasonText;

    @TableField("reason_pic")
    private String reasonPic;
}