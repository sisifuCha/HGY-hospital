package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.dto.HistoryScheduleDTO;
import com.example.pojo.dto.ScheduleDTO;
import com.example.pojo.entity.DoctorSchedule;
import com.example.pojo.vo.FinalScheduleVO;
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

    @Select("SELECT" +
            " u.name AS doc_name," +
            "ds.left_source_count AS left_source_count," +
            "t.name AS title_name," +
            "ds.template_id AS template_id," +
            "ds.schedule_date AS schedule_date," +
            "dp.name AS depart_name " +
            "FROM " +
            "\"user\" u INNER JOIN doctor d ON d.id=u.id " +
            "INNER JOIN department dp ON dp.id=d.depart_id " +
            "INNER JOIN doc_schedule_record ds ON ds.doc_id=d.id " +
            "INNER JOIN title_number_source t ON t.id=d.doc_title_id " +
            "WHERE ds.schedule_date <= #{Sun} AND ds.schedule_date >= #{Mon} " +
            "AND dp.name = #{depart_name}")
    @ResultMap("FinalScheduleVOMap")
    List<FinalScheduleVO> getScheduleHistory(LocalDate Mon, LocalDate Sun, String depart_name);

    @Select("SELECT id FROM doc_schedule_record")
    List<String> getIdList();
}