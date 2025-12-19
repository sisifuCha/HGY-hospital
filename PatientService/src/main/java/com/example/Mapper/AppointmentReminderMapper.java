package com.example.Mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 就诊提醒Mapper
 * 用于查询需要发送就诊提醒的挂号记录
 */
@Mapper
public interface AppointmentReminderMapper {

    /**
     * 查询1天后需要就诊的患者信息（上午场次）
     * 在每天8:00:00触发
     */
    @Select({
        "SELECT ",
        "  rr.patient_id,",
        "  p.name AS patient_name,",
        "  u.phone_num AS phone,",
        "  dsr.schedule_date,",
        "  st.start_time,",
        "  st.time_period_name,",
        "  doc_u.name AS doctor_name,",
        "  dep.name AS department_name,",
        "  c.clinic_number,",
        "  c.location AS clinic_location",
        "FROM register_record rr",
        "JOIN patient p ON rr.patient_id = p.id",
        "JOIN \"user\" u ON p.id = u.id",
        "JOIN doc_schedule_record dsr ON rr.sch_id = dsr.id",
        "JOIN schedule_template st ON dsr.template_id = st.id",
        "JOIN doctor doc ON dsr.doc_id = doc.id",
        "JOIN \"user\" doc_u ON doc.id = doc_u.id",
        "JOIN department dep ON doc.depart_id = dep.id",
        "LEFT JOIN clinic c ON doc.clinic_id = c.id",
        "WHERE dsr.schedule_date = #{targetDate}",
        "  AND st.start_time = '08:00:00'",
        "  AND rr.status IN ('已预约', '已挂号')"
    })
    List<Map<String, Object>> findOneDayBeforeMorningAppointments(@Param("targetDate") LocalDate targetDate);

    /**
     * 查询1天后需要就诊的患者信息（下午场次）
     * 在每天13:30:00触发
     */
    @Select({
        "SELECT ",
        "  rr.patient_id,",
        "  p.name AS patient_name,",
        "  u.phone_num AS phone,",
        "  dsr.schedule_date,",
        "  st.start_time,",
        "  st.time_period_name,",
        "  doc_u.name AS doctor_name,",
        "  dep.name AS department_name,",
        "  c.clinic_number,",
        "  c.location AS clinic_location",
        "FROM register_record rr",
        "JOIN patient p ON rr.patient_id = p.id",
        "JOIN \"user\" u ON p.id = u.id",
        "JOIN doc_schedule_record dsr ON rr.sch_id = dsr.id",
        "JOIN schedule_template st ON dsr.template_id = st.id",
        "JOIN doctor doc ON dsr.doc_id = doc.id",
        "JOIN \"user\" doc_u ON doc.id = doc_u.id",
        "JOIN department dep ON doc.depart_id = dep.id",
        "LEFT JOIN clinic c ON doc.clinic_id = c.id",
        "WHERE dsr.schedule_date = #{targetDate}",
        "  AND st.start_time = '13:30:00'",
        "  AND rr.status IN ('已预约', '已挂号')"
    })
    List<Map<String, Object>> findOneDayBeforeAfternoonAppointments(@Param("targetDate") LocalDate targetDate);

    /**
     * 查询3小时后需要就诊的患者信息（上午场次）
     * 在每天5:00:00触发，查询8:00:00开始的就诊
     */
    @Select({
        "SELECT ",
        "  rr.patient_id,",
        "  p.name AS patient_name,",
        "  u.phone_num AS phone,",
        "  dsr.schedule_date,",
        "  st.start_time,",
        "  st.time_period_name,",
        "  doc_u.name AS doctor_name,",
        "  dep.name AS department_name,",
        "  c.clinic_number,",
        "  c.location AS clinic_location",
        "FROM register_record rr",
        "JOIN patient p ON rr.patient_id = p.id",
        "JOIN \"user\" u ON p.id = u.id",
        "JOIN doc_schedule_record dsr ON rr.sch_id = dsr.id",
        "JOIN schedule_template st ON dsr.template_id = st.id",
        "JOIN doctor doc ON dsr.doc_id = doc.id",
        "JOIN \"user\" doc_u ON doc.id = doc_u.id",
        "JOIN department dep ON doc.depart_id = dep.id",
        "LEFT JOIN clinic c ON doc.clinic_id = c.id",
        "WHERE dsr.schedule_date = #{targetDate}",
        "  AND st.start_time = '08:00:00'",
        "  AND rr.status IN ('已预约', '已挂号')"
    })
    List<Map<String, Object>> findThreeHoursBeforeMorningAppointments(@Param("targetDate") LocalDate targetDate);

    /**
     * 查询3小时后需要就诊的患者信息（下午场次）
     * 在每天10:30:00触发，查询13:30:00开始的就诊
     */
    @Select({
        "SELECT ",
        "  rr.patient_id,",
        "  p.name AS patient_name,",
        "  u.phone_num AS phone,",
        "  dsr.schedule_date,",
        "  st.start_time,",
        "  st.time_period_name,",
        "  doc_u.name AS doctor_name,",
        "  dep.name AS department_name,",
        "  c.clinic_number,",
        "  c.location AS clinic_location",
        "FROM register_record rr",
        "JOIN patient p ON rr.patient_id = p.id",
        "JOIN \"user\" u ON p.id = u.id",
        "JOIN doc_schedule_record dsr ON rr.sch_id = dsr.id",
        "JOIN schedule_template st ON dsr.template_id = st.id",
        "JOIN doctor doc ON dsr.doc_id = doc.id",
        "JOIN \"user\" doc_u ON doc.id = doc_u.id",
        "JOIN department dep ON doc.depart_id = dep.id",
        "LEFT JOIN clinic c ON doc.clinic_id = c.id",
        "WHERE dsr.schedule_date = #{targetDate}",
        "  AND st.start_time = '13:30:00'",
        "  AND rr.status IN ('已预约', '已挂号')"
    })
    List<Map<String, Object>> findThreeHoursBeforeAfternoonAppointments(@Param("targetDate") LocalDate targetDate);

    /**
     * 插入就诊提醒消息到message_record表
     */
    @Insert({
        "INSERT INTO message_record (",
        "  title, content, sender_type, receiver_type, receiver_id, status, read_status, created_time, over_time",
        ") VALUES (",
        "  #{title}, #{content}, 'system', 'specific_patient', #{receiverId}, 'unsent', 'unconfirmed', #{createdTime}, #{overTime}",
        ")"
    })
    int insertReminderMessage(
        @Param("title") String title,
        @Param("content") String content,
        @Param("receiverId") String receiverId,
        @Param("createdTime") LocalDateTime createdTime,
        @Param("overTime") LocalDateTime overTime
    );
}
