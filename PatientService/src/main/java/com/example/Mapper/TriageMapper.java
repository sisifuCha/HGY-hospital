package com.example.Mapper;

import com.example.dto.TriageRecordRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TriageMapper {
    int insertTriageRecord(@Param("record") TriageRecordRow record);

    TriageRecordRow selectByTriageId(@Param("triageId") String triageId);

    List<TriageRecordRow> fetchHistory(@Param("patientId") String patientId);
}
