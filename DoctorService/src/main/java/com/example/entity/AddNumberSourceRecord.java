package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("add_number_source_record")
public class AddNumberSourceRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "patient_id", type = IdType.INPUT)
    private String patientId;

    @TableField("sch_id")
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