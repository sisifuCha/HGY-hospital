package com.example.Controller;

import com.example.Service.DepartmentService;
import com.example.conmon.result.Result;
import com.example.pojo.entity.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/list")
    public Result<List<Department>> list() {
        List<Department> list = departmentService.getAllDepartmentsWithHierarchy();
        return Result.success(list);
    }
}

