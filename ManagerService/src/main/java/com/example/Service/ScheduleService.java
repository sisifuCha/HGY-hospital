package com.example.Service;

import com.example.Conmon.result.Result;
import com.example.pojo.dto.NextWeekScheduleDTO;
import com.example.pojo.dto.ScheduleDTO;
import com.example.pojo.entity.DoctorSchedule;

public interface ScheduleService {

    public Result<String> createSchedule(DoctorSchedule schedule);

    public Result<String> createSchedules(NextWeekScheduleDTO nextWeekScheduleDTO);
}
