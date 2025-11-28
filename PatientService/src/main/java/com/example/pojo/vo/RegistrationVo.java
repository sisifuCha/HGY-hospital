package com.example.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistrationVo {

    private String patientId;
    private String patientName;
    private String scheduleRecordId;
    private String doctorId;
    private String doctorName;
    private String departmentId;
    private String departmentName;
    private String clinicId;
    private String clinicName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate registrationDate;

    @JsonProperty("timeSlot")
    private String timePeriodName;

    private BigDecimal registrationFee;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime registerTime;
}
