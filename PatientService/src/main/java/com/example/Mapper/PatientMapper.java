package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.Patient;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者数据访问层
 * 继承自 Mybatis-Plus 的 BaseMapper，提供了基础的 CRUD 功能
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
    // 可以在此添加针对 Patient 表的特定查询方法
}

