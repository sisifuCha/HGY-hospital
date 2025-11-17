package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.Department;
import com.example.pojo.entity.DoctorSchedule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

public interface DepartmentMapper extends BaseMapper<Department> {

    @Select("SELECT id FROM department WHERE name = #{name}")
        // 或者如果MyBatis-Plus能自动找到，可直接写resultMap的id
    String getIdByName(@Param("name") String name);
}
