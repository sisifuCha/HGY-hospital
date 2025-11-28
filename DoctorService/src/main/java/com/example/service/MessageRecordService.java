package com.example.service;

import com.example.entity.MessageRecord;

import java.util.List;

/**
 * 消息记录服务接口
 */
public interface MessageRecordService {

    /**
     * 创建加号申请消息
     * @param patientName 患者姓名
     * @param docId 医生ID
     * @return 创建的消息记录
     */
    MessageRecord createAddNumberApplicationMessage(String patientName, String docId);

    /**
     * 获取医生的未发送消息列表
     * @param docId 医生ID
     * @return 消息列表
     */
    List<MessageRecord> getUnsentMessagesForDoctor(String docId);

    /**
     * 获取医生的所有有效消息列表（未过期）
     * @param docId 医生ID
     * @return 消息列表
     */
    List<MessageRecord> getActiveMessagesForDoctor(String docId);

    /**
     * 标记消息为已发送
     * @param messageId 消息ID
     * @return 是否更新成功
     */
    boolean markAsSent(Integer messageId);

    /**
     * 批量标记消息为已发送
     * @param messageIds 消息ID列表
     * @return 更新的记录数
     */
    int batchMarkAsSent(List<Integer> messageIds);

    /**
     * 标记消息为已确认阅读
     * @param messageId 消息ID
     * @return 是否更新成功
     */
    boolean markAsConfirmed(Integer messageId);
}
