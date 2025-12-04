package com.example.pojo.vo;

import java.util.ArrayList;
import java.util.List;

import com.example.pojo.dto.AdjustItemDTO;

import lombok.Data;

@Data
public class AdjustItemsVO {
    Integer page;
    Integer pageSize;
    // 修改后
    List<AdjustItemDTO> items = new ArrayList<>();
}