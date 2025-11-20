package com.example.Service;

import com.example.Conmon.result.Result;
import com.example.Mapper.DepartmentMapper;
import com.example.Mapper.DoctorMapper;
import com.example.Mapper.ScheduleMapper;
import com.example.pojo.dto.HistoryScheduleDTO;
import com.example.pojo.dto.NextWeekScheduleDTO;
import com.example.pojo.dto.ScheduleDTO;
import com.example.pojo.entity.DoctorSchedule;
import com.example.pojo.vo.HistoryScheduleWeekVO;
import com.example.pojo.vo.ScheduleWeekVO;
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
    private DepartmentMapper departmentMapper;
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
    public Result<Void> createSchedules(NextWeekScheduleDTO nextWeekScheduleDTO, Integer week) {
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
                            executeScheduleDate(doctorSchedule,fieldName,week);
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

    @Override
    public Result<Void> deleteSchedule(LocalDate date, String doctor_name, String template_id, String depart_name) {

        try {
            String depart_id = departmentMapper.getIdByName(depart_name);
            String doc_id = doctorMapper.getIdByNameAndDepart(doctor_name, depart_id);
            int res = scheduleMapper.deleteSchedule(date, template_id, doc_id);
            if (res > 0) {return Result.success(null);}
        } catch (Exception e) {
            return Result.fail("数据库错误");
        }
        return Result.fail("删除项不存在");
    }

    @Override
    public Result<HistoryScheduleWeekVO> getScheduleHistory(LocalDate date, String depart_name) {
        HistoryScheduleWeekVO historyScheduleWeekVO = new HistoryScheduleWeekVO();
        //获取date所在周的周一和周六时间
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        LocalDate sunday = date.with(DayOfWeek.SUNDAY);
        List<HistoryScheduleDTO> doctorSchedules = scheduleMapper.getScheduleHistory(monday,sunday,depart_name);
        //对实体做视图转换
        Map<LocalDate,String> dayMapping = new HashMap<>();
        dayMapping.put(date.with(DayOfWeek.MONDAY), "Mon");    // 周一
        dayMapping.put(date.with(DayOfWeek.TUESDAY), "Tue");   // 周二
        dayMapping.put(date.with(DayOfWeek.WEDNESDAY), "Wed"); // 周三
        dayMapping.put(date.with(DayOfWeek.THURSDAY), "Thu");  // 周四
        dayMapping.put(date.with(DayOfWeek.FRIDAY), "Fri");    // 周五
        dayMapping.put(date.with(DayOfWeek.SATURDAY), "Sat");  // 周六
        dayMapping.put(date.with(DayOfWeek.SUNDAY), "Sun");    // 周日
        //做周中分类
        for(HistoryScheduleDTO dto:doctorSchedules){
            LocalDate currentDate = dto.getDate();
            if (dayMapping.containsKey(currentDate)) {
                String dayOfWeek = dayMapping.get(currentDate);
                ScheduleDTO scheduleDTO = new ScheduleDTO();
                scheduleDTO.setDoctor_name(dto.getDoctor_name());
                scheduleDTO.setTemplate_id(dto.getTemplate_id());

                // 使用反射获取目标属性并添加对象
                try {
                    // 1. 获取目标类的Class对象
                    Class<?> targetClass = historyScheduleWeekVO.getClass();

                    // 2. 根据属性名（dayOfWeek的值）获取对应的Field对象
                    Field targetField = targetClass.getDeclaredField(dayOfWeek);

                    // 3. 设置可访问性（防止因private修饰而无法访问）
                    targetField.setAccessible(true);

                    // 4. 获取该属性在当前对象中的值（即您需要的集合）
                    Object fieldValue = targetField.get(historyScheduleWeekVO);

                    // 5. 安全检查：确保获取到的是一个List集合
                    if (fieldValue instanceof List) {
                        // 6. 将scheduleDTO添加到集合中
                        @SuppressWarnings("unchecked")
                        List<ScheduleDTO> targetList = (List<ScheduleDTO>) fieldValue;
                        targetList.add(scheduleDTO);
                    } else {
                        // 处理类型不匹配的情况，例如记录错误日志
                        System.err.println("错误: 属性 " + dayOfWeek + " 不是List类型或为null。");
                    }

                } catch (NoSuchFieldException e) {
                    System.err.println("错误: 在类 " + " 中未找到名为 '" + dayOfWeek + "' 的属性。");
                } catch (IllegalAccessException e) {
                    System.err.println("错误: 无法访问属性 '" + dayOfWeek + "'。");
                }
            }
        }
        return Result.success(historyScheduleWeekVO);
    }
    /******************************************************/
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
            } else{
                // 获取下周对应日期的日期[1](@ref)
                LocalDate thisWeekDay = LocalDate.now().with(TemporalAdjusters.previousOrSame(targetDayOfWeek));
                targetDate = thisWeekDay.plusWeeks(code);
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
