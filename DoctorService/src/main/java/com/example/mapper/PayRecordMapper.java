package com.example.mapper;

import com.example.entity.PayRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface PayRecordMapper {

    @Insert("INSERT INTO pay_record (id, pay_time, pay_status, ori_amount, ask_pay_amount, patient_id, doc_id) " +
            "VALUES (#{id}, #{payTime}, #{payStatus}, #{oriAmount}, #{askPayAmount}, #{patientId}, #{docId})")
    int insertPayRecord(PayRecord payRecord);

    @Select("SELECT ori_cost FROM title_number_source WHERE id = #{titleId}")
    BigDecimal getTitleOriCost(String titleId);

    @Select("SELECT percent FROM reimburse_type WHERE id = (" +
            "SELECT reimburse_id FROM patient WHERE id = #{patientId})")
    Integer getPatientReimbursePercent(String patientId);
}
