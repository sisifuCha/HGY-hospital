package com.example.Mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.pojo.entity.Doctor;
import com.example.pojo.entity.DoctorSchedule;
import com.example.pojo.vo.FinalScheduleVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


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

    // 根据职称名称查询职称ID
    @Select("SELECT id FROM title_number_source WHERE name = #{name}")
    String getTitleIdByName(@Param("name") String name);

    //分页获取医生信息
    IPage<Doctor> selectDoctorPage(IPage<Doctor> page, @Param("ew") QueryWrapper<Doctor> queryWrapper);
    
    @Select("select d.id as userId, u.name as userName, dep.name as department " +
            "from doctor d left join \"user\" u on d.id = u.id " +
            "left join department dep on d.depart_id = dep.id")
    List<Map<String, String>> getDoctorOptions();

    //根据时间范围和科室获取排班信息
    @Select("SELECT u.name AS doc_name," +
            "ds.left_source_count AS left_source_count," +
            "t.name AS title_name," +
            "ds.doc_id AS doc_id," +
            "ds.id AS id," +
            "ds.template_id AS template_id," +
            "ds.schedule_date AS schedule_date," +
            "ds.status AS status," +
            "dp.name AS depart_name " +
            "FROM " +
            "\"user\" u INNER JOIN doctor d ON d.id=u.id " +
            "INNER JOIN department dp ON dp.id=d.depart_id " +
            "INNER JOIN doc_schedule_record ds ON ds.doc_id=d.id " +
            "INNER JOIN title_number_source t ON t.id=d.doc_title_id " +
            "WHERE ds.schedule_date <= #{endTime} AND ds.schedule_date >= #{startTime} " +
            "AND dp.id = #{departId}")
    @ResultMap("FinalScheduleVOMap")
    // 或者如果MyBatis-Plus能自动找到，可直接写resultMap的id
    List<FinalScheduleVO> selectDoctorSchedule(@Param("startTime") LocalDate startTime, @Param("endTime") LocalDate endTime, @Param("departId") String departId);
    
    // 根据科室名称获取该科室的所有医生
    @Select("SELECT u.id AS userid, u.name AS username, dep.name AS department " +
            "FROM \"user\" u " +
            "INNER JOIN \"doctor\" d ON u.id = d.id " +
            "INNER JOIN \"department\" dep ON d.depart_id = dep.id " +
            "WHERE dep.name = #{departName} " +
            "ORDER BY u.name")
    List<Map<String, String>> getDoctorsByDepartment(@Param("departName") String departName);
}