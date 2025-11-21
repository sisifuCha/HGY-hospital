package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.dto.HistoryScheduleDTO;
import com.example.pojo.dto.ScheduleDTO;
import com.example.pojo.entity.DoctorSchedule;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<DoctorSchedule> {

    @Select("SELECT id FROM doc_schedule_record ORDER BY ctid DESC LIMIT 1")
    public String getMaxId ();

    int insertBatch(@Param("list") List<DoctorSchedule> scheduleList);

    @Delete("DELETE * FROM 'doc_schedule_record' where schedule_date >= #{Mon} AND schedule_date <= #{Sun}")
    int deleteBatch(LocalDate Mon, LocalDate Sun);

    @Delete("DELETE FROM doc_schedule_record where schedule_date=#{date} and template_id=#{template_id} and doc_id=#{doc_id}")
    int deleteSchedule(LocalDate date,String template_id,String doc_id);

    @Select("SELECT u.name, ds.schedule_date, ds.template_id " +
            "FROM doc_schedule_record ds INNER JOIN \"user\" u ON u.id = ds.doc_id " +
            "INNER JOIN doctor d ON d.id = u.id " +
            "INNER JOIN department dep ON d.depart_id = dep.id " +
            "WHERE ds.schedule_date >= #{Mon} AND ds.schedule_date <= #{Sun} " +
            "AND dep.name = #{depart_name}") // 确保参数化查询
    @ResultMap("HistoryScheduleDTOMap")
    List<HistoryScheduleDTO> getScheduleHistory(LocalDate Mon, LocalDate Sun, String depart_name);

    @Select("SELECT id FROM doc_schedule_record")
    List<String> getIdList();
}