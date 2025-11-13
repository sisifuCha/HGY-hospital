package com.example.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class DepartmentWithSubDepartmentsDto {
    private String id;
    private String name;
    private List<SubDepartmentDto> subDepartments;
}

