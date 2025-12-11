package com.example.Mapper;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DataPlatformMapper {

    // 获取本周的预约挂号人数
    @Select("SELECT COUNT(*) as count FROM register_record WHERE status IN ('已挂号', '就诊中', '已就诊') AND TO_CHAR(register_time, 'IW') = TO_CHAR(CURRENT_DATE, 'IW')")
    Integer getWeeklyRegistrationCount();

    // 获取本周的就诊人数
    @Select("SELECT COUNT(*) as count FROM register_record WHERE status = '已就诊' AND TO_CHAR(register_time, 'IW') = TO_CHAR(CURRENT_DATE, 'IW')")
    Integer getWeeklyVisitCount();

    // 获取科室就诊人数
    @Select("SELECT de.name as dep_name, COUNT(*) as count FROM register_record rr JOIN doc_schedule_record dsr ON rr.sch_id = dsr.id JOIN doctor d ON dsr.doc_id = d.id JOIN department de ON d.depart_id = de.id WHERE rr.status = '已就诊' AND TO_CHAR(rr.register_time, 'IW') = TO_CHAR(CURRENT_DATE, 'IW') GROUP BY de.name")
    List<Map<String, Object>> getDepartmentVisitCount();

    // 获取医生就诊人数
    @Select("SELECT u.name, COUNT(*) as count FROM register_record rr JOIN doc_schedule_record dsr ON rr.sch_id = dsr.id JOIN doctor d ON dsr.doc_id = d.id JOIN \"user\" u ON d.id = u.id WHERE rr.status = '已就诊' AND TO_CHAR(rr.register_time, 'IW') = TO_CHAR(CURRENT_DATE, 'IW') GROUP BY u.name")
    List<Map<String, Object>> getDoctorVisitCount();

    // 获取医生排班量
    @Select("SELECT u.name, COUNT(*) as count FROM doc_schedule_record dsr JOIN doctor d ON dsr.doc_id = d.id JOIN \"user\" u ON d.id = u.id WHERE TO_CHAR(dsr.schedule_date, 'IW') = TO_CHAR(CURRENT_DATE, 'IW') GROUP BY u.name")
    List<Map<String, Object>> getDoctorScheduleCount();
    
    // 获取今日挂号人数
    @Select("SELECT COUNT(*) as count FROM register_record WHERE TO_CHAR(register_time, 'YYYY-MM-DD') = TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD')")
    Integer getTodayRegisterCount();
    
    // 获取今日已经就诊人数
    @Select("SELECT COUNT(*) as count FROM register_record WHERE status = '已就诊' AND TO_CHAR(register_time, 'YYYY-MM-DD') = TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD')")
    Integer getTodayVisitedCount();
    
    // 获取今日正在排队的患者人数
    @Select("SELECT COUNT(*) as count FROM register_record WHERE status = '已挂号' AND TO_CHAR(register_time, 'YYYY-MM-DD') = TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD')")
    Integer getTodayLiningCount();
}