package com.example.Controller;

import com.example.Conmon.result.Result;
import com.example.Service.ScheduleService;
import com.example.pojo.dto.DelayBatchDTO;
import com.example.pojo.dto.HistoryScheduleDTO;
import com.example.pojo.dto.NextWeekScheduleDTO;
import com.example.pojo.dto.StopBatchScheduleDTO;
import com.example.pojo.vo.AdjustItemsVO;
import com.example.pojo.vo.FinalScheduleWeekVO;
import com.example.pojo.vo.HistoryScheduleWeekVO;
import com.example.pojo.vo.ScheduleWeekVO;

import io.swagger.v3.oas.models.security.SecurityScheme.In;

import org.apache.ibatis.javassist.tools.framedump;
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
    public Result<Void> CreateNextWeekSchedule(@RequestBody() NextWeekScheduleDTO nextWeekScheduleDTO,
            @RequestParam("week") Integer week) {
        System.out.println(nextWeekScheduleDTO);
        return scheduleService.createSchedules(nextWeekScheduleDTO, week);
    }

    @DeleteMapping("/DeleteSchedule")
    public Result<Void> DeleteSchedule(@RequestParam("date") LocalDate date,
            @RequestParam("doctor_name") String doctor_name,
            @RequestParam("template_id") String template_id,
            @RequestParam("depart_name") String depart_name) {
        System.out.println("收到了删除下周排班的请求,日期为" + date.toString());
        return scheduleService.deleteSchedule(date, doctor_name, template_id, depart_name);
    }

    @GetMapping("/GetSchedulesHistory")
    public Result<FinalScheduleWeekVO> getSchedulesHistory(@RequestParam LocalDate date,
            @RequestParam String depart_name) {
        System.out.println("收到了获取值班历史的请求" + date.toString() + depart_name);
        return scheduleService.getScheduleHistory(date, depart_name);
    }

    @PostMapping("/stopSingle/Schedule")
    public Result<Void> stopSingle(@RequestParam("schedule_id") String schedule_id,
            @RequestParam("reason") String reason) {
        System.out.println("收到了中止排班的请求");
        return scheduleService.stopSingle(schedule_id, reason);
    }

    @PostMapping("/stopBatchSchedule")
    public Result<Void> stopBatch(@RequestBody() StopBatchScheduleDTO stopBatchScheduleDTO) {
        return scheduleService.stopBatch(stopBatchScheduleDTO);
    }

    @PostMapping("/batchDelay")
    public Result<Void> batchDelay(@RequestBody() DelayBatchDTO delayBatchDTO) {
        return scheduleService.delayBatch(delayBatchDTO);
    }

    @GetMapping("/shift-requests")
    public Result<AdjustItemsVO> getShiftRequests(@RequestParam() Integer page,
            @RequestParam() Integer pageSize,
            @RequestParam() String status,
            @RequestParam(required = false) String doc_id,
            @RequestParam(required = false) LocalDate targetDateFrom,
            @RequestParam(required = false) LocalDate targetDateTo,
            @RequestParam() String type) {
        // TODO 实现服务
        // 'SHIFT_CHANGE' | 'LEAVE' |'ALL';
        System.out.println("收到了获取排班调整请求的请求");
        Integer type_int = null;
        switch (type) {
            case "ALL":
                break;
            case "SHIFT_CHANGE":
                type_int = 0;
                break;
            default:
                type_int = 1;
                break;
        }
        return scheduleService.getShiftRequests(status, doc_id, targetDateFrom, targetDateTo, type_int, page, pageSize);
    }

    @PatchMapping("/shift-requests/{id}")
    public Result<Void> handleShiftRequest(@PathVariable String id,
            @RequestBody java.util.Map<String, String> requestBody) {
        System.out.println("收到了处理排班调整请求: id=" + id + ", action=" + requestBody.get("action"));
        String action = requestBody.get("action");
        return scheduleService.handleShiftRequest(id, action);
    }

}
