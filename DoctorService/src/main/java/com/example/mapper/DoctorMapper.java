package com.example.mapper;

import com.example.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DoctorMapper {
    @Select("SELECT d.*, u.\"name\", u.\"phone_num\", u.\"email\", u.\"sex\", " +
           "c.\"clinic_number\", c.\"location\", dep.\"name\" as department_name, " +
           "tns.\"number_source_count\", tns.\"ori_cost\" " +
           "FROM \"doctor\" d " +
           "JOIN \"user\" u ON d.\"ID\" = u.\"ID\" " +
           "JOIN \"clinic\" c ON d.\"clinic_ID\" = c.\"ID\" " +
           "JOIN \"department\" dep ON c.\"dep_ID\" = dep.\"ID\" " +
           "LEFT JOIN \"title_number_source\" tns ON d.\"doc_title_ID\" = tns.\"ID\" " +
           "WHERE d.\"ID\" = #{docId}")
    Doctor getDoctorWithDetails(@Param("docId") String docId);

    @Select("SELECT d.*, u.\"name\", u.\"pass\" " +
           "FROM \"doctor\" d " +
           "JOIN \"user\" u ON d.\"id\" = u.\"id\" " +
           "WHERE u.\"account\" = #{docAccount}")
    Doctor getDoctorByAccount(@Param("docAccount") String docAccount);

    @Select("SELECT d.*, u.\"name\" " +
           "FROM \"doctor\" d " +
           "JOIN \"user\" u ON d.\"ID\" = u.\"ID\" " +
           "JOIN \"clinic\" c ON d.\"clinic_ID\" = c.\"ID\" " +
           "JOIN \"department\" dep ON c.\"dep_ID\" = dep.\"ID\" " +
           "WHERE dep.\"ID\" = (SELECT c2.\"dep_ID\" FROM \"clinic\" c2 " +
           "JOIN \"doctor\" d2 ON c2.\"ID\" = d2.\"clinic_ID\" " +
           "WHERE d2.\"ID\" = #{docId})")
    java.util.List<Doctor> getDoctorsByDepartment(@Param("docId") String docId);
}