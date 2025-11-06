package com.example.Mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.pojo.entity.Doctor;
import com.example.pojo.entity.DoctorSchedule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;


public interface DoctorMapper extends BaseMapper<Doctor> {

    // 更新医生信息（同时更新user表和doctor表）
    int updateDoctor(Doctor doctor);

    // 根据ID查询完整医生信息（联表查询）
    Doctor selectById(String id);

    // 检查账号名是否存在
    int checkAccountNameExists(@Param("accountName") String accountName, String userId);

    //分页获取医生信息
    IPage<Doctor> selectDoctorPage(IPage<Doctor> page, @Param("ew") QueryWrapper<Doctor> queryWrapper);

    // 根据科室ID查询医生列表（新增方法）
    List<Doctor> selectByDepartmentId(@Param("clinicId") String clinicId);

    //根据时间范围和科室获取排班信息
    @Select("SELECT ds.schedule_id, ds.doctor_id, ds.schedule_time_id, ds.date, ds.available_slots " +
            "FROM doctor_schedule ds INNER JOIN doctor d ON ds.doctor_id = d.id " +
            "WHERE ds.date >= #{startTime} AND ds.date <= #{endTime} AND d.depart_id = #{departId}")
    @ResultMap("DoctorScheduleResultMap")
    // 或者如果MyBatis-Plus能自动找到，可直接写resultMap的id
    List<DoctorSchedule> selectDoctorSchedule(@Param("startTime") LocalDate startTime, @Param("endTime") LocalDate endTime, @Param("departId") String departId);
}