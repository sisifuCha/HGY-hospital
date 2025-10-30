package com.example.mapper;

import com.example.entity.RegisterRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RegisterRecordMapper {
    @Select("SELECT rr.*, u.\"name\" as patient_name, u.\"sex\", u.\"phone_num\" " +
           "FROM \"register_record\" rr " +
           "JOIN \"doc_schedule_record\" dsr ON rr.\"sch_ID\" = dsr.\"ID\" " +
           "JOIN \"patient\" p ON rr.\"patient_ID\" = p.\"ID\" " +
           "JOIN \"user\" u ON p.\"ID\" = u.\"ID\" " +
           "WHERE dsr.\"doc_ID\" = #{docId} AND rr.\"status\" != 'cancelled'")
    List<RegisterRecord> getPatientList(@Param("docId") String docId);

    @Select("SELECT rr.*, u.\"name\" as patient_name, u.\"sex\", u.\"phone_num\", " +
           "dep.\"name\" as department_name, doc_u.\"name\" as doctor_name " +
           "FROM \"register_record\" rr " +
           "JOIN \"patient\" p ON rr.\"patient_ID\" = p.\"ID\" " +
           "JOIN \"user\" u ON p.\"ID\" = u.\"ID\" " +
           "JOIN \"doc_schedule_record\" dsr ON rr.\"sch_ID\" = dsr.\"ID\" " +
           "JOIN \"doctor\" d ON dsr.\"doc_ID\" = d.\"ID\" " +
           "JOIN \"user\" doc_u ON d.\"ID\" = doc_u.\"ID\" " +
           "JOIN \"clinic\" c ON d.\"clinic_ID\" = c.\"ID\" " +
           "JOIN \"department\" dep ON c.\"dep_ID\" = dep.\"ID\" " +
           "WHERE rr.\"patient_ID\" = #{patientId}")
    List<RegisterRecord> getPatientHistory(@Param("patientId") String patientId);

    @Update("UPDATE \"register_record\" SET \"status\" = #{status} " +
           "WHERE \"patient_ID\" = #{patientId} AND \"sch_ID\" = #{schId}")
    int updateStatus(@Param("patientId") String patientId, 
                    @Param("schId") String schId, 
                    @Param("status") String status);
}