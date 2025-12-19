package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 导诊响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriageResponse {
    
    private List<Recommendation> recommendations;
    private List<String> suggestedActions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private String departmentId;
        private String departmentName;
        private Double confidence;
        private String reason;
    }
}
