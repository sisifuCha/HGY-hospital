package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("patient")
public class Patient {
    private String id;
    private Date birth;
    @TableField("id_num")
    private String idNum;
    @TableField("medical_insuranceid")
    private String medicalInsuranceId;
    @TableField("reimburse_id")
    private String reimburseId;
}