package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.DoctorSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ScheduleMapper extends BaseMapper<DoctorSchedule> {
    @Select("SELECT id FROM doc_schedule_record ORDER BY ctid DESC LIMIT 1")
    public String getMaxId ();
}