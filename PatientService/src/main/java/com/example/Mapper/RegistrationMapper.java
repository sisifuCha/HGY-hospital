package com.example.Mapper;

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
}

