package com.example.Service;

import com.example.Mapper.DepartmentMapper;
import com.example.pojo.entity.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public List<Department> getAllDepartmentsWithHierarchy() {
        return departmentMapper.findAllWithSubDepartments();
    }
}

