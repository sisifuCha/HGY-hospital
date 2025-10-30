package com.example.Mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.pojo.entity.Doctor;
import org.apache.ibatis.annotations.Param;
import java.util.List;


public interface DoctorMapper extends BaseMapper<Doctor> {

    // 更新医生信息（同时更新user表和doctor表）
    int updateDoctor(Doctor doctor);

    // 根据ID查询完整医生信息（联表查询）
    Doctor selectById(String id);

    // 检查账号名是否存在
    int checkAccountNameExists(@Param("accountName") String accountName, String userId);

    IPage<Doctor> selectDoctorPage(IPage<Doctor> page, @Param("ew") QueryWrapper<Doctor> queryWrapper);
}