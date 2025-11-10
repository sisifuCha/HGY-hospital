package com.example.Controller;

import com.example.Conmon.result.Result;
import com.example.Service.ScheduleService;
import com.example.pojo.dto.NextWeekScheduleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/CreateNextWeekSchedule")
    public Result<String> CreateNextWeekSchedule(@RequestBody() NextWeekScheduleDTO nextWeekScheduleDTO){
        System.out.println(nextWeekScheduleDTO);
        return null;
    }
}
