package com.example.pojo.vo;

import lombok.Data;

@Data
public class HotDataPlatformVO {
    // 今日挂号人数
    private Integer register_num;
    
    // 今日已经就诊人数
    private Integer visited_num;
    
    // 今日正在排队的患者人数
    private Integer lining_num;
}