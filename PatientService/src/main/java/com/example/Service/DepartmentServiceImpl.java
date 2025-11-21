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
        // 1. 查询出所有一级科室（根科室）
        List<Department> rootDepartments = departmentMapper.findAllRootDepartments();

        // 2. 遍历一级科室，为每个一级科室查询并设置其二级子科室列表
        if (rootDepartments != null && !rootDepartments.isEmpty()) {
            for (Department root : rootDepartments) {
                // 根据父科室ID查询子科室
                List<Department> subDepartments = departmentMapper.findSubDepartmentsByFatherId(root.getId());
                // 设置子科室列表
                root.setSubDepartments(subDepartments);
            }
        }

        return rootDepartments;
    }
}

