package com.example.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 加号申请状态响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddNumberStatusDto {
    
    /**
     * 加号申请ID（由patientId和scheduleRecordId组合）
     */
    private String addNumberId;
    
    /**
     * 患者ID
     */
    private String patientId;
    
    /**
     * 排班记录ID
     */
    private String scheduleRecordId;
    
    /**
     * 医生姓名
     */
    private String doctorName;
    
    /**
     * 科室名称
     */
    private String departmentName;
    
    /**
     * 门诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date scheduleDate;
    
    /**
     * 时段名称
     */
    private String timePeriodName;
    
    /**
     * 审核状态：待审核/已同意/已拒绝
     */
    private String status;
    
    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "GMT+8")
    private Date applyTime;
    
    /**
     * 申请原因文本
     */
    private String reasonText;
    
    /**
     * 申请原因图片
     */
    private String reasonPic;
}
