package com.example.Controller;

import com.example.Conmon.result.Result;
import com.example.Service.ScheduleService;
import com.example.pojo.dto.NextWeekScheduleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/CreateNextWeekSchedule")
    public Result<Void> CreateNextWeekSchedule(@RequestBody() NextWeekScheduleDTO nextWeekScheduleDTO){
        System.out.println(nextWeekScheduleDTO);
        return scheduleService.createSchedules(nextWeekScheduleDTO);
    }

    @DeleteMapping("/DeleteSchedule")
    public Result<Void> DeleteSchedule(@RequestParam("date")LocalDate date,
                                       @RequestParam("doctor_name" ) String doctor_name,
                                       @RequestParam("template_id") String template_id,
                                       @RequestParam("depart_name") String depart_name){
        System.out.println("收到了删除下周排班的请求,日期为"+date.toString());
        return scheduleService.deleteSchedule(date,doctor_name,template_id,depart_name);
    }
}
