package com.example.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 评价请求
 */
@Data
public class FeedbackRequest {
    
    @NotBlank(message = "患者ID不能为空")
    private String patientId;
    
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer score;
    
    @Size(max = 10, message = "标签数量不能超过10个")
    private List<String> tags;
    
    @Size(max = 500, message = "评论内容不能超过500字")
    private String comment;
}
