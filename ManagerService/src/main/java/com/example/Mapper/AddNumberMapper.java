package com.example.Mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface AddNumberMapper {
    // 获取加号请求列表
    @Select("SELECT patient_id, sch_id, apply_time, status, reason_text FROM add_number_source_record LIMIT #{pageSize} OFFSET #{offset}")
    List<Map<String, Object>> getAddedSource(@Param("pageSize") Integer pageSize, @Param("offset") Integer offset);
    
    // 获取加号请求总记录数
    @Select("SELECT COUNT(*) FROM add_number_source_record")
    int getAddedSourceCount();
    
    // 审批加号请求
    @Update("UPDATE add_number_source_record SET status = #{status} WHERE patient_id = #{patientId} AND sch_id = #{schId}")
    int updateAddedSourceStatus(@Param("patientId") String patientId, @Param("schId") String schId, @Param("status") String status);
    
    // 在挂号表中新增条目
    @Insert("INSERT INTO register_record (patient_id, sch_id, register_time, status) VALUES (#{patientId}, #{schId}, NOW(), '已挂号')")
    int addRegisterRecord(@Param("patientId") String patientId, @Param("schId") String schId);
}