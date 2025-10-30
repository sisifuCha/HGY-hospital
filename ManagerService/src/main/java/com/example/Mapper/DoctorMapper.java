package com.example.Mapper;

import com.example.pojo.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DoctorMapper {
    /**
     * 根据ID更新医生信息
     */
    int updateDoctor(Doctor doctor);

    /**
     * 根据ID查询医生信息
     */
    Doctor selectById(String id);

    /**
     * 检查账号名是否存在（排除当前医生）
     */
    int checkAccountNameExists(@Param("accountName") String accountName,@Param("id") String id);
}