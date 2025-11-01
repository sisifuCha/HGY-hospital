package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorsRequestDTO {

    private String filter_name;

    private String filter_value;

    private Integer page;

    private Integer num;
}
