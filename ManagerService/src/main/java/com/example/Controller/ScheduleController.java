package com.example.Controller;

import com.example.Conmon.result.Result;
import com.example.Service.ScheduleService;
import com.example.pojo.dto.HistoryScheduleDTO;
import com.example.pojo.dto.NextWeekScheduleDTO;
import com.example.pojo.vo.HistoryScheduleWeekVO;
import com.example.pojo.vo.ScheduleWeekVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/CreateNextWeekSchedule")
    public Result<Void> CreateNextWeekSchedule(@RequestBody() NextWeekScheduleDTO nextWeekScheduleDTO,@RequestParam("week") Integer week){
        System.out.println(nextWeekScheduleDTO);
        return scheduleService.createSchedules(nextWeekScheduleDTO,week);
    }

    @DeleteMapping("/DeleteSchedule")
    public Result<Void> DeleteSchedule(@RequestParam("date")LocalDate date,
                                       @RequestParam("doctor_name" ) String doctor_name,
                                       @RequestParam("template_id") String template_id,
                                       @RequestParam("depart_name") String depart_name){
        System.out.println("收到了删除下周排班的请求,日期为"+date.toString());
        return scheduleService.deleteSchedule(date,doctor_name,template_id,depart_name);
    }

    @GetMapping("/GetSchedulesHistory")
    public Result<HistoryScheduleWeekVO> getSchedulesHistory(LocalDate date , String depart_name){
        System.out.println("收到了获取值班历史的请求"+date.toString()+depart_name);
        return scheduleService.getScheduleHistory(date,depart_name);
    }
}
