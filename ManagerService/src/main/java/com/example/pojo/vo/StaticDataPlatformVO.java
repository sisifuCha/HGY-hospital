package com.example.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class StaticDataPlatformVO {
    // 本周已预约人数
    private Integer regis_count;

    // 本周已就诊人数
    private Integer visitor_count;

    // 各科室就诊人数
    private List<DepVisitorCount> dep_visitor_count;

    // 各医生就诊人数
    private List<DocVisitorCount> doc_visitor_count;

    // 各医生排班量
    private List<DocScheduleCount> doc_schedule_count;

    // 科室就诊人数内部类
    @Data
    public static class DepVisitorCount {
        private String dep_name;
        private Long count;
    }

    // 医生就诊人数内部类
    @Data
    public static class DocVisitorCount {
        private String doc_name;
        private Long count;
    }

    // 医生排班量内部类
    @Data
    public static class DocScheduleCount {
        private String doc_name;
        private Long count;
    }
}