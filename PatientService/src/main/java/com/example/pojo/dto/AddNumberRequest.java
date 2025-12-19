package com.example.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 患者提交加号申请请求
 */
@Data
public class AddNumberRequest {
    
    /**
     * 患者ID
     */
    @NotBlank(message = "患者ID不能为空")
    private String patientId;
    
    /**
     * 排班记录ID
     */
    @NotBlank(message = "排班记录ID不能为空")
    private String scheduleRecordId;
    
    /**
     * 申请原因文本
     */
    @NotBlank(message = "申请原因不能为空")
    @Size(max = 500, message = "申请原因不能超过500字")
    private String reasonText;
    
    /**
     * 申请原因图片（可选，Base64编码）
     */
    @Size(max = 500000, message = "图片大小不能超过500KB")
    private String reasonPic;
}
