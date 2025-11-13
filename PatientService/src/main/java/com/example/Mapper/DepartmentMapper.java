package com.example.Mapper;

import com.example.pojo.dto.DepartmentWithSubDepartmentsDto;
import com.example.pojo.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper {
    /**
     * 查询所有一级科室（根科室）
     * @return
     */
    List<Department> findAllRootDepartments();

    /**
     * 根据父科室ID查询所有子科室
     * @param fatherId
     * @return
     */
    List<Department> findSubDepartmentsByFatherId(@Param("fatherId") String fatherId);

    /**
     * 查询所有一级科室及其子科室（DTO版，用于对外API）
     * @return
     */
    List<DepartmentWithSubDepartmentsDto> findAllDepartmentsWithSubDepartments();
}
