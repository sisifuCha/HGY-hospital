package com.example.Service;

import com.example.Conmon.result.Result;
import com.example.Mapper.ScheduleMapper;
import com.example.pojo.dto.NextWeekScheduleDTO;
import com.example.pojo.dto.ScheduleDTO;
import com.example.pojo.entity.DoctorSchedule;
import com.example.utils.ScheduleIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;


    @Override
    public Result<String> createSchedule(DoctorSchedule schedule) {
        // 生成ID
        String newId = ScheduleIdGenerator.getNextId();
        // 设置到实体对象
        schedule.setSchedule_id(newId);

        // 调用MyBatis-Plus的insert方法
        int result = scheduleMapper.insert(schedule);
        if (result > 0) {
            return Result.success("排班创建成功，记录ID为：" + newId);
        } else {
            return Result.fail("排班创建失败。");
        }
    }

    @Override
    public Result<String> createSchedules(NextWeekScheduleDTO nextWeekScheduleDTO) {
        List<DoctorSchedule> schedules = new ArrayList<>();
        //遍历nextWeekScheduleDTO的属性，打印出属性名字
        return null;
    }

    private void executeScheduleDate(DoctorSchedule doctorSchedule, String date, Integer code) {
        // 创建日期缩写与DayOfWeek的映射关系[1,4](@ref)
        Map<String, DayOfWeek> dayMapping = new HashMap<>();
        dayMapping.put("mon", DayOfWeek.MONDAY);    // 周一
        dayMapping.put("tue", DayOfWeek.TUESDAY);   // 周二
        dayMapping.put("wed", DayOfWeek.WEDNESDAY); // 周三
        dayMapping.put("thu", DayOfWeek.THURSDAY);  // 周四
        dayMapping.put("fri", DayOfWeek.FRIDAY);    // 周五
        dayMapping.put("sat", DayOfWeek.SATURDAY);  // 周六
        dayMapping.put("sun", DayOfWeek.SUNDAY);    // 周日

        if (dayMapping.containsKey(date)) {
            DayOfWeek targetDayOfWeek = dayMapping.get(date);
            LocalDate targetDate;

            if (code == 0) {
                // 获取本周对应日期的日期[1](@ref)
                targetDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(targetDayOfWeek));
            } else if (code == 1) {
                // 获取下周对应日期的日期[1](@ref)
                LocalDate thisWeekDay = LocalDate.now().with(TemporalAdjusters.previousOrSame(targetDayOfWeek));
                targetDate = thisWeekDay.plusWeeks(1);
            } else {
                // 如果code不是0或1，设置为当前日期
                targetDate = LocalDate.now();
            }

            // 设置日期到doctorSchedule对象
            doctorSchedule.setDate(targetDate);
        } else {
            // 如果传入的日期缩写不合法，可以记录日志或抛出异常
            // 这里设置为当前日期作为默认值
            doctorSchedule.setDate(LocalDate.now());
        }
    }
}
