package com.example.Controller;

import com.example.Service.WaitingService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.CreateWaitingRequest;
import com.example.pojo.dto.WaitingDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registrations/waiting")
public class WaitingController {

    @Autowired
    private WaitingService waitingService;

    @PostMapping
    public Result<?> createWaiting(@RequestBody @Valid CreateWaitingRequest req) {
        WaitingDto dto = waitingService.createWaiting(req.getPatientId(), req.getScheduleRecordId());
        return Result.success(dto);
    }

    @GetMapping
    public Result<?> getWaitingBySchedule(@RequestParam String scheduleRecordId) {
        List<WaitingDto> list = waitingService.getWaitingListBySchedule(scheduleRecordId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("scheduleRecordId", scheduleRecordId);
        resp.put("waitingCount", list.size());
        resp.put("waitingList", list);
        return Result.success(resp);
    }

    @GetMapping("/patient")
    public Result<?> getWaitingByPatient(@RequestParam String patientId, @RequestParam(required = false) String date) {
        List<WaitingDto> list = waitingService.getWaitingListByPatient(patientId, date);
        Map<String, Object> resp = new HashMap<>();
        resp.put("patientId", patientId);
        resp.put("items", list);
        return Result.success(resp);
    }

    @DeleteMapping
    public Result<?> cancelWaiting(@RequestParam String patientId, @RequestParam String waitingId) {
        WaitingDto dto = waitingService.cancelWaiting(patientId, waitingId);
        return Result.success(dto);
    }

    @PostMapping("/confirm")
    public Result<?> confirmWaiting(@RequestBody Map<String, String> body) {
        String waitingId = body.get("waitingId");
        WaitingDto dto = waitingService.confirmWaiting(waitingId);
        return Result.success(dto);
    }
}

