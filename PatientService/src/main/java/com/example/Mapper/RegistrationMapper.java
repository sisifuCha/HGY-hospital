package com.example.Mapper;

import com.example.pojo.dto.RegistrationDto;
import com.example.pojo.dto.DoctorWithSchedulesDto;
import com.example.pojo.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface RegistrationMapper {
    List<DoctorWithSchedulesDto> findDoctorsWithSchedulesByDepartmentAndDate(@Param("departmentId") String departmentId, @Param("date") LocalDate date);
    Doctor findDoctorDetailsById(@Param("doctorId") String doctorId);
    Integer countActiveRegistrationByKey(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId);
    int decrementScheduleLeftSource(@Param("scheduleRecordId") String scheduleRecordId);
    int incrementScheduleLeftSource(@Param("scheduleRecordId") String scheduleRecordId);
    int insertRegistration(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId, @Param("status") String status);
    String getRegistrationStatusByKey(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId);
    int updateRegistrationStatusToCanceled(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId);
    RegistrationDto findRegistrationByKey(@Param("patientId") String patientId, @Param("scheduleRecordId") String scheduleRecordId);
}
