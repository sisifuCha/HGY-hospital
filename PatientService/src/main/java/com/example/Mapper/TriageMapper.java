package com.example.Mapper;

import com.example.dto.TriageRequest;
import com.example.dto.TriageResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TriageMapper {
    TriageResponse generateSuggestions(@Param("request") TriageRequest request);
    List<TriageResponse> fetchHistory(@Param("patientId") String patientId);
}
