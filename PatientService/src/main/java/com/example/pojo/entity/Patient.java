package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 患者实体类，对应数据库的 patient 表。
 * 注意：当前项目数据库脚本（dataBaseSolution/hospital_register_postgresql_17.sql）中 patient 表字段为：
 * - id (PK)
 * - birth
 * - ID_num
 * - medical_insuranceID
 * - reimburse_ID
 */
@Data
@TableName("patient")
public class Patient {

    /**
     * 患者ID（同时关联 user.id）
     */
    @TableId("id")
    private String patientId;

    /**
     * 出生日期（脚本字段名为 birth）
     */
    @TableField(value = "birth", insertStrategy = FieldStrategy.ALWAYS)
    private Date birthday;

    /**
     * 身份证号（脚本字段名为 ID_num）
     */
    @TableField("ID_num")
    private String identificationId;

    /**
     * 医保ID（脚本字段名为 medical_insuranceID）
     */
    @TableField("medical_insuranceID")
    private String patientInsurId;

    /**
     * 报销类型ID（脚本字段名为 reimburse_ID）
     */
    @TableField("reimburse_ID")
    private String reimbId;

    /**
     * 档案电话（扩展脚本新增字段，展示优先级高于 user.phone_num）
     */
    @TableField("phone")
    private String phone;

    /**
     * 地址（扩展脚本新增字段）
     */
    @TableField("address")
    private String address;

    /**
     * 病史（扩展脚本新增字段）
     */
    @TableField("medical_history")
    private String medicalHistory;

    /**
     * 过敏史（扩展脚本新增字段）
     */
    @TableField("allergies")
    private String allergies;
}
