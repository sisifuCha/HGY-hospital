package com.example.Service;

import com.example.pojo.dto.WaitingDto;
import com.example.pojo.dto.WaitingRuleDto;

import java.util.List;

public interface WaitingService {
    WaitingDto createWaiting(String patientId, String scheduleRecordId);
    List<WaitingDto> getWaitingListBySchedule(String scheduleRecordId);
    List<WaitingDto> getWaitingListByPatient(String patientId, String date);
    WaitingDto cancelWaiting(String patientId, String scheduleRecordId);
    WaitingDto confirmWaiting(String waitingId);
    List<WaitingRuleDto> getWaitingRules();
}

