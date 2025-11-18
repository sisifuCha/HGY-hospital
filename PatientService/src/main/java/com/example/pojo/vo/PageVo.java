package com.example.pojo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVo<T> {
    private int page;
    private int pageSize;
    private long total;
    @JsonProperty("items")
    private List<T> list;
}
