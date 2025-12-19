package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感操作记录实体
 */
@Data
@TableName("sensitive_operation")
public class SensitiveOperation {
    
    @TableId("id")
    private String id;
    
    @TableField("patient_id")
    private String patientId;
    
    @TableField("sensitive_op_type")
    private String sensitiveOpType;
    
    @TableField("op_time")
    private LocalDateTime opTime;
    
    @TableField("remark")
    private String remark;
}
