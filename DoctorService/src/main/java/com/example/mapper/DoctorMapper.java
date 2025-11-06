package com.example.mapper;

import com.example.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DoctorMapper {
    @Select("SELECT d.*, u.\"name\", u.\"phone_num\", u.\"email\", u.\"sex\", " +
           "c.\"clinic_number\", c.\"location\", dep.\"name\" as department_name, " +
           "tns.\"number_source_count\", tns.\"ori_cost\", tns.\"name\" AS title_name " +
           "FROM \"doctor\" d " +
           "JOIN \"user\" u ON d.\"id\" = u.\"id\" " +
           "JOIN \"clinic\" c ON d.\"clinic_id\" = c.\"id\" " +
           "JOIN \"department\" dep ON c.\"dep_id\" = dep.\"id\" " +
           "LEFT JOIN \"title_number_source\" tns ON d.\"doc_title_id\" = tns.\"id\" " +
           "WHERE d.\"id\" = #{docId}")
    Doctor getDoctorWithDetails(@Param("docId") String docId);

    @Select("SELECT d.*, u.\"name\", u.\"pass\" " +
           "FROM \"doctor\" d " +
           "JOIN \"user\" u ON d.\"id\" = u.\"id\" " +
           "WHERE u.\"account\" = #{docAccount}")
    Doctor getDoctorByAccount(@Param("docAccount") String docAccount);

    @Select("SELECT d.*, u.\"name\" " +
           "FROM \"doctor\" d " +
           "JOIN \"user\" u ON d.\"id\" = u.\"id\" " +
           "JOIN \"clinic\" c ON d.\"clinic_id\" = c.\"id\" " +
           "JOIN \"department\" dep ON c.\"dep_id\" = dep.\"id\" " +
           "WHERE dep.\"id\" = (SELECT c2.\"dep_id\" FROM \"clinic\" c2 " +
           "JOIN \"doctor\" d2 ON c2.\"id\" = d2.\"clinic_id\" " +
           "WHERE d2.\"id\" = #{docId})")
    java.util.List<Doctor> getDoctorsByDepartment(@Param("docId") String docId);

       @Update({
              "UPDATE \"doctor\" SET \"status\" = #{status} WHERE \"id\" = #{docId}"
       })
       int updateDoctorStatus(@Param("docId") String docId, @Param("status") String status);

       @Update({
              "<script>",
              "UPDATE \"user\"",
              "<set>",
              "  <if test='name != null'>\"name\" = #{name},</if>",
              "  <if test='email != null'>\"email\" = #{email},</if>",
              "  <if test='phone != null'>\"phone_num\" = #{phone},</if>",
              "</set>",
              "WHERE \"id\" = #{doctorId}",
              "</script>"
       })
       int updateUserProfile(@Param("doctorId") String doctorId,
                                            @Param("name") String name,
                                            @Param("email") String email,
                                            @Param("phone") String phone);

       @Update({
              "<script>",
              "UPDATE \"doctor\"",
              "<set>",
              "  <if test='clinicId != null'>\"clinic_id\" = #{clinicId},</if>",
              "  <if test='titleId != null'>\"doc_title_id\" = #{titleId},</if>",
              "</set>",
              "WHERE \"id\" = #{doctorId}",
              "</script>"
       })
       int updateDoctorProfile(@Param("doctorId") String doctorId,
                                                 @Param("clinicId") String clinicId,
                                                 @Param("titleId") String titleId);
}