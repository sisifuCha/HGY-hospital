package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.dto.AdjustItemDTO;
import com.example.pojo.entity.DoctorSchedule;
import com.example.pojo.vo.FinalScheduleVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map; // 添加Map导入以支持调班申请详情查询

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
        int updateShiftRequest(String id, String action);

        /**
         * 查询调班申请的详细信息
         * 用于获取调班申请的完整数据，支持审批业务逻辑处理
         * 
         * @param id 调班申请ID
         * @return 调班申请详细信息的Map集合
         */
        @Select("SELECT * FROM doc_schedule_change_record WHERE ori_sch_id = #{id}")
        Map<String, Object> getShiftRequestDetail(String id);

        /**
         * 更新排班记录的状态
         * 用于调班审批通过后更新原排班的状态
         * 
         * @param scheduleId 排班ID
         * @param status     新状态
         * @return 更新结果（影响行数）
         */
        @Update("UPDATE doc_schedule_record SET status = #{status} WHERE schedule_id = #{scheduleId}")
        int updateScheduleStatus(@Param("scheduleId") String scheduleId, @Param("status") String status);

        /**
         * 更新排班记录的日期和时间段
         * 用于调班类型的审批通过后，将原排班调整到新的时间
         * 
         * @param oriScheId  原排班ID
         * @param date       新日期
         * @param templateId 新的时间模板ID
         * @return 更新结果（影响行数）
         */
        @Update("UPDATE doc_schedule_record SET schedule_date = #{date}, template_id = #{templateId} WHERE schedule_id = #{oriScheId}")
        int updateScheduleTime(@Param("oriScheId") String oriScheId, @Param("date") String date,
                        @Param("templateId") String templateId);

        /**
         * 根据ID删除排班记录
         * 用于请假类型的审批通过后，删除原排班记录
         * 
         * @param scheduleId 排班ID
         * @return 删除结果（影响行数）
         */
        @Delete("DELETE FROM doc_schedule_record WHERE schedule_id = #{scheduleId}")
        int deleteScheduleById(String scheduleId);
}