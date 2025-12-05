package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.dto.AdjustItemDTO;
import com.example.pojo.entity.DoctorSchedule;
import com.example.pojo.vo.FinalScheduleVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<DoctorSchedule> {

        @Select("SELECT id FROM doc_schedule_record ORDER BY ctid DESC LIMIT 1")
        public String getMaxId();

        int insertBatch(@Param("list") List<DoctorSchedule> scheduleList);

        @Delete("DELETE * FROM 'doc_schedule_record' where schedule_date >= #{Mon} AND schedule_date <= #{Sun}")
        int deleteBatch(LocalDate Mon, LocalDate Sun);

        @Delete("DELETE FROM doc_schedule_record where schedule_date=#{date} and template_id=#{template_id} and doc_id=#{doc_id}")
        int deleteSchedule(LocalDate date, String template_id, String doc_id);

        @Select("SELECT" +
                        " u.name AS doc_name," +
                        "ds.left_source_count AS left_source_count," +
                        "t.name AS title_name," +
                        "ds.template_id AS template_id," +
                        "ds.schedule_date AS schedule_date," +
                        "ds.id AS schedule_id," +
                        "ds.status AS status," +
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

        @Update("UPDATE doc_schedule_record SET reason = #{reason},status = 1 WHERE id = #{schedule_id}")
        int stopSingle(String schedule_id, String reason);

        int stopBatch(@Param("doctorIds") List<String> doc_ids,
                        @Param("reason") String reason,
                        @Param("startTemplate") String start_template,
                        @Param("endTemplate") String end_template,
                        @Param("startDate") LocalDate start_date,
                        @Param("endDate") LocalDate end_date);

        int delayBatch(@Param("doctorIds") List<String> doc_ids,
                        @Param("reason") String reason,
                        @Param("startTemplate") String start_template,
                        @Param("endTemplate") String end_template,
                        @Param("startDate") LocalDate start_date,
                        @Param("endDate") LocalDate end_date,
                        @Param("delay_days") Integer delay_days);

        @Select("SELECT " +
                        "ori_sch_id," + "doc_id," +
                        "u.name AS name," +
                        "target_sch_id AS target_sche_id," +
                        "reason_text," +
                        "status," +
                        "target_date," +
                        "type " +
                        "FROM doc_schedule_change_record " +
                        "INNER JOIN \"user\" u ON u.id=doc_schedule_change_record.doc_id " +
                        "WHERE (status = #{status}::varchar(20) OR #{status}::varchar(20) IS NULL) " +
                        "AND (doc_id = #{doc_id}::varchar(20) OR #{doc_id}::varchar(20) IS NULL) " +
                        "AND (target_date >= #{targetDateFrom}::date OR #{targetDateFrom}::date IS NULL) " +
                        "AND (target_date <= #{targetDateTo}::date OR #{targetDateTo}::date IS NULL) " +
                        "AND (type = #{type}::integer OR #{type}::integer IS NULL) " +
                        "ORDER BY ori_sch_id DESC " +
                        "LIMIT #{pageSize} OFFSET #{pageSize} * (#{page} - 1)")
        @ResultMap("AdjustItemDTOMap")
        List<AdjustItemDTO> getShiftRequests(@Param("status") String status,
                        @Param("doc_id") String doc_id,
                        @Param("targetDateFrom") LocalDate targetDateFrom,
                        @Param("targetDateTo") LocalDate targetDateTo,
                        @Param("type") Integer type,
                        @Param("page") Integer page,
                        @Param("pageSize") Integer pageSize);

        @Update("UPDATE doc_schedule_change_record SET status = #{action} WHERE ori_sch_id = #{id}")
        int updateShiftRequest(String id,String action);
}