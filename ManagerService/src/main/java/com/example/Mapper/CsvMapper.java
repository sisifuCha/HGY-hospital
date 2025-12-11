package com.example.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface CsvMapper {
    List<Map<String, Object>> getDoctorInfo();
    List<Map<String, Object>> getPatientInfo();
    List<Map<String, Object>> getRegistrationRecord(@Param("patientId") String patientId, @Param("begin") String begin, @Param("end") String end);
    List<Map<String, Object>> getScheduleRecord(@Param("doctorId") String doctorId, @Param("begin") String begin, @Param("end") String end);
}
