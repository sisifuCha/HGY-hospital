package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 加号申请记录实体类
 */
@Data
@TableName("add_number_source_record")
public class AddNumberSourceRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 患者ID（联合主键）
     */
    @TableId(value = "patient_id", type = IdType.INPUT)
    private String patientId;

    /**
     * 排班ID（联合主键）
     */
    @TableField("sch_id")
    private String schId;

    /**
     * 申请时间
     */
    @TableField("apply_time")
    private Date applyTime;

    /**
     * 审核状态：待审核/已同意/已拒绝
     */
    @TableField("status")
    private String status;

    /**
     * 申请原因文本
     */
    @TableField("reason_text")
    private String reasonText;

    /**
     * 申请原因图片（可选）
     */
    @TableField("reason_pic")
    private String reasonPic;
}
