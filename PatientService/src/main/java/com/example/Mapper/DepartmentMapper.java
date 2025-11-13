package com.example.Mapper;

import com.example.pojo.entity.Department;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentMapper {
    List<Department> findAllWithSubDepartments();
}

