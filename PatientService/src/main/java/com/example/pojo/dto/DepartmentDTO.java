package com.example.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class DepartmentDTO {
    private String id;
    private String name;
    private List<SubDepartment> subDepartments;

    @Data
    public static class SubDepartment {
        private String id;
        private String name;
    }
}

