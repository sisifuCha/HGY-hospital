package com.example.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageRecord {
    private Integer id;
    private String title;
    private String content;
    private String senderType;        // system, admin
    private String receiverType;      // specific_patient, specific_doctor, department_doctors, all_doctors, all_patients, specific_group
    private String receiverId;        // 接收者ID
    private String status;            // unsent, sent, expired
    private String readStatus;        // unconfirmed, confirmed
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private LocalDateTime overTime;   // 过期时间
}
