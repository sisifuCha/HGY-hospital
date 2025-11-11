package com.example.Service;

import com.example.Conmon.result.Result;
import com.example.Mapper.DoctorMapper;
import com.example.Mapper.ScheduleMapper;
import com.example.pojo.dto.NextWeekScheduleDTO;
import com.example.pojo.dto.ScheduleDTO;
import com.example.pojo.entity.DoctorSchedule;
import com.example.utils.ScheduleIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private ScheduleIdGenerator scheduleIdGenerator;

    @Override
    public Result<Void> createSchedule(DoctorSchedule schedule) {
        // 调用MyBatis-Plus的insert方法
        int result = scheduleMapper.insert(schedule);
        if (result > 0) {
            return Result.success("排班创建成功，记录ID为：" + schedule.getSchedule_id(),null);
        } else {
            return Result.fail("排班创建失败。");
        }
    }

    @Override
    public Result<Void> createSchedules(NextWeekScheduleDTO nextWeekScheduleDTO) {
        List<DoctorSchedule> schedules = new ArrayList<>();
        //遍历nextWeekScheduleDTO的属性，处理其中的scheduleDTO对象
        //java的反射机制，运行时获取类的信息
        try {
            // 获取对象的Class对象
            Class<?> clazz = nextWeekScheduleDTO.getClass();

            // 获取对象的所有属性（包括私有属性）
            Field[] fields = clazz.getDeclaredFields();

            // 遍历属性并打印名称和值
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldValue = field.get(nextWeekScheduleDTO);

                // 处理属性值为null的情况
                if (fieldValue == null) {
                    System.out.println(fieldName + ": null");
                    continue;
                }

                // 安全地处理可能的集合类型
                if (fieldValue instanceof Collection) {
                    Collection<?> collection = (Collection<?>) fieldValue;
                    for (Object item : collection) {
                        if (item instanceof ScheduleDTO) {
                            ScheduleDTO dto = (ScheduleDTO) item;
                            // 处理每个ScheduleDTO
                            DoctorSchedule doctorSchedule = new DoctorSchedule();
                            doctorSchedule.setDoctor_id(doctorMapper.getIdByName(dto.getDoctor_name()));
                            doctorSchedule.setSchedule_id(scheduleIdGenerator.getNextId());
                            doctorSchedule.setSchedule_time_id(dto.getTemplate_id());
                            executeScheduleDate(doctorSchedule,fieldName,0);
                            schedules.add(doctorSchedule);
                        } else {
                            System.out.println("集合中包含非ScheduleDTO对象: " + item);
                        }
                    }
                } else {
                    // 非集合类型的普通属性
                    System.out.println(fieldName + ": " + fieldValue);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            // 根据实际需求处理异常，这里返回错误结果
            return Result.fail("遍历对象属性时发生错误");
        }
        try{
            for (DoctorSchedule item : schedules) {
                createSchedule(item);
            }
            return Result.success("插入成功",null);
        }catch (Exception e){
            e.printStackTrace();
            return Result.fail("新排班插入数据库错误");
        }
    }

    private void executeScheduleDate(DoctorSchedule doctorSchedule, String date, Integer code) {
        // 创建日期缩写与DayOfWeek的映射关系
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
