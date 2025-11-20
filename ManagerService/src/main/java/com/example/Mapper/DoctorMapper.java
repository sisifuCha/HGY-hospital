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

    @Select("SELECT u.id FROM \"user\" u INNER JOIN \"doctor\" d ON d.id=u.id WHERE u.name = #{name} AND d.depart_id=#{departId}")
    String getIdByNameAndDepart(String name,String departId);

    @Select("SELECT id from \"user\" where name = #{name};")
    String getIdByName(@Param("name") String name);

    // 更新医生信息（同时更新user表和doctor表）
    int updateDoctor(Doctor doctor);

    // 根据ID查询完整医生信息（联表查询）
    Doctor selectById(String id);

    // 检查账号名是否存在
    int checkAccountNameExists(@Param("accountName") String accountName, String userId);

    //分页获取医生信息
    IPage<Doctor> selectDoctorPage(IPage<Doctor> page, @Param("ew") QueryWrapper<Doctor> queryWrapper);

    //根据时间范围和科室获取排班信息
    @Select("SELECT ds.id, ds.doc_id, ds.template_id, ds.schedule_date, ds.left_source_count " +
            "FROM doc_schedule_record ds INNER JOIN doctor d ON ds.doc_id = d.id " +
            "WHERE ds.schedule_date >= #{startTime} AND ds.schedule_date <= #{endTime} AND d.depart_id = #{departId}")
    @ResultMap("DoctorScheduleResultMap")
    // 或者如果MyBatis-Plus能自动找到，可直接写resultMap的id
    List<DoctorSchedule> selectDoctorSchedule(@Param("startTime") LocalDate startTime, @Param("endTime") LocalDate endTime, @Param("departId") String departId);
}