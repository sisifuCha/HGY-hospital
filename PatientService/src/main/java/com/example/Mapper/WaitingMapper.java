package com.example.Mapper;

import com.example.pojo.dto.WaitingDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WaitingMapper {
    int insertWaiting(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId);
    WaitingDto findWaitingByPatientAndSchedule(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId);
    WaitingDto findWaitingById(@Param("waitingId") String waitingId);
    int countActiveWaitingByKey(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId);
    int countWaitingByPatientAndDate(@Param("patientId") String patientId, @Param("date") String date);
    int countWaitingBefore(@Param("scheduleRecordId") String scheduleRecordId, @Param("patientId") String patientId);
    List<WaitingDto> findWaitingsBySchedule(@Param("scheduleRecordId") String scheduleRecordId);
    List<WaitingDto> findWaitingsByPatient(@Param("patientId") String patientId, @Param("date") String date);
    int updateWaitingStatusToCanceled(@Param("waitingId") String waitingId);
    int updateWaitingStatusToConfirmed(@Param("waitingId") String waitingId, @Param("registrationId") String registrationId);

    // helpers
    Integer countScheduleRecordById(@Param("scheduleRecordId") String scheduleRecordId);
    Integer getScheduleLeftSource(@Param("scheduleRecordId") String scheduleRecordId);
}

