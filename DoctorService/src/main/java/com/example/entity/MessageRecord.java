package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息记录实体类
 * 对应 message_record 表
 */
@Data
@TableName("message_record")
public class MessageRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 消息标题
     */
    @TableField("title")
    private String title;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 发送方类型：system：系统, admin：管理员, server：服务器
     */
    @TableField("sender_type")
    private String senderType;

    /**
     * 接收方类型：specific_patient，specific_doctor，department_doctors，all_doctors，all_patients，specific_group
     */
    @TableField("receiver_type")
    private String receiverType;

    /**
     * 接收者ID，当接收方为特定用户时使用
     */
    @TableField("receiver_id")
    private String receiverId;

    /**
     * 消息状态：unsent：未发送, sent：已发送, expired：已过期
     */
    @TableField("status")
    private String status;

    /**
     * 阅读状态：unconfirmed：未确认, confirmed：已确认
     */
    @TableField("read_status")
    private String readStatus;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    private LocalDateTime updatedTime;

    /**
     * 过期时间，到期后状态自动变更为已过期
     */
    @TableField("over_time")
    private LocalDateTime overTime;
}
