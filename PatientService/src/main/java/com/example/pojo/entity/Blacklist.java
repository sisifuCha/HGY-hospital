package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 黑名单实体
 */
@Data
@TableName("blacklist")
public class Blacklist {
    
    @TableId("id")
    private String id;
    
    @TableField("sen_id1")
    private String senId1;
    
    @TableField("sen_id2")
    private String senId2;
    
    @TableField("sen_id3")
    private String senId3;
    
    @TableField("count")
    private Short count;
}
