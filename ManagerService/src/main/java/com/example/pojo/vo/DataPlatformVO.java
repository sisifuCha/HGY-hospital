package com.example.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class DataPlatformVO {
    private Integer regis_count;
    private Integer visitor_count;
    private List<DepVisitorCount> dep_visitor_count;
    private List<DocVisitorCount> doc_visitor_count;
    private List<DocScheduleCount> doc_schedule_count;

    @Data
    public static class DepVisitorCount {
        private String name;
        private Integer num;
    }

    @Data
    public static class DocVisitorCount {
        private String name;
        private Integer num;
    }

    @Data
    public static class DocScheduleCount {
        private String name;
        private String num;
    }
}