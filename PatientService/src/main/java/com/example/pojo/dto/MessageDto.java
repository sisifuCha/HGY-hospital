package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private Long id;
    private String title;
    private String content;
    private String status; // unsent, sent
    private String createdTime;
    private String receiverId; // 添加 receiverId
}
