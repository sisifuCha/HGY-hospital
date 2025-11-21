package com.example.Service;

import com.example.pojo.dto.WaitingDto;

import java.util.List;

public interface WaitingService {
    WaitingDto createWaiting(String patientId, String scheduleRecordId);
    List<WaitingDto> getWaitingListBySchedule(String scheduleRecordId);
    List<WaitingDto> getWaitingListByPatient(String patientId, String date);
    WaitingDto cancelWaiting(String patientId, String waitingId);
    WaitingDto confirmWaiting(String waitingId);
}

