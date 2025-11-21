package com.example.Service;

import com.example.pojo.entity.Department;

import java.util.List;

public interface DepartmentService {
    List<Department> getAllDepartmentsWithHierarchy();
}

