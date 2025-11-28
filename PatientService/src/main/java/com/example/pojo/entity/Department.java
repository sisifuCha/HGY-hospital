
package com.example.pojo.entity;

import lombok.Data;
import java.util.List;

@Data
public class Department {
    private String id;
    private String name;
    private List<Department> subDepartments;
}

