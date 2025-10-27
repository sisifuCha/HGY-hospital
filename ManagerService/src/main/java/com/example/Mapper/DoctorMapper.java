package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.Doctor;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface DoctorMapper extends BaseMapper<Doctor> {

    // 更新医生信息（同时更新user表和doctor表）
    int updateDoctor(Doctor doctor);

    // 根据ID查询完整医生信息（联表查询）
    Doctor selectById(String id);

    // 检查账号名是否存在
    int checkAccountNameExists(@Param("accountName") String accountName, String userId);

    // 查询所有医生的完整信息
    List<Doctor> selectAllFullDoctors();

    // 根据条件动态查询医生信息
    List<Doctor> selectDoctorsByCondition(Map<String, Object> condition);
}