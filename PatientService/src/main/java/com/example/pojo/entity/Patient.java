package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 患者实体类，对应数据库的 "patient" 表
 * 包含患者的特有信息
 */
@Data
@TableName("patient")
public class Patient {
    // 患者ID，同时也是外键，关联到 user 表的 id
    @TableId("id")
    private String patientId;

    // 出生日期
    @TableField(value = "birth", insertStrategy = FieldStrategy.ALWAYS)
    private Date birthday;

    // 身份证号
    @TableField("id_num")
    private String identificationId;

    // 关联的医保ID
    @TableField("medical_insuranceid")
    private String patientInsurId;

    // 关联的报销类型ID
    @TableField("reimburse_id")
    private String reimbId;
}
