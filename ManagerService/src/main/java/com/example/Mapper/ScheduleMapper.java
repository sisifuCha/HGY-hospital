package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.DoctorSchedule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}