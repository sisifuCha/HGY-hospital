package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true) // 【关键】让Lombok在生成方法时包含父类字段
@TableName("`doctor`") // 指定此类映射到 doctor 表
public class Doctor extends User { // 继承 BaseUser

    @TableField("doc_title_id")
    private String titleId;

    @TableField("clinic_id")
    private String clinicId;

    @TableField("status")
    private String doctorStatus;

    @TableField("details")
    private String doctorDetails;

    @TableField("speciality")
    private String doctorSpeciality;

    @TableField("depart_id")
    private String doctorDepartId;

}