package com.example.Service;

import com.example.Conmon.result.Result;
import com.example.pojo.dto.NextWeekScheduleDTO;
import com.example.pojo.dto.ScheduleDTO;
import com.example.pojo.entity.DoctorSchedule;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

public interface ScheduleService {

    public Result<Void> createSchedule(DoctorSchedule schedule);

    public Result<Void> createSchedules(NextWeekScheduleDTO nextWeekScheduleDTO);

    //删除下周的排班
    public Result<Void> deleteSchedule(LocalDate date, String doctor_name, String template_id, String depart_name);
}
