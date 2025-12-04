package com.example.service;

import com.example.entity.MessageRecord;

import java.util.List;

/**
 * 消息队列服务接口
 * 基于 Redis 实现医生端消息队列
 */
public interface MessageQueueService {

    /**
     * 初始化消息队列
     * 从数据库加载所有未发送的消息到 Redis
     */
    void initializeQueue();

    /**
     * 将消息添加到队列
     * @param docId 医生ID
     * @param messageId 消息ID
     */
    void enqueueMessage(String docId, Integer messageId);

    /**
     * 批量将消息添加到队列
     * @param docId 医生ID
     * @param messageIds 消息ID列表
     */
    void enqueueMessages(String docId, List<Integer> messageIds);

    /**
     * 获取医生的所有待发送消息
     * @param docId 医生ID
     * @return 消息列表
     */
    List<MessageRecord> getQueuedMessages(String docId);

    /**
     * 从队列中移除消息
     * @param docId 医生ID
     * @param messageId 消息ID
     */
    void dequeueMessage(String docId, Integer messageId);

    /**
     * 批量从队列中移除消息
     * @param docId 医生ID
     * @param messageIds 消息ID列表
     */
    void dequeueMessages(String docId, List<Integer> messageIds);

    /**
     * 清空医生的消息队列
     * @param docId 医生ID
     */
    void clearQueue(String docId);

    /**
     * 获取队列中消息数量
     * @param docId 医生ID
     * @return 消息数量
     */
    Long getQueueSize(String docId);

    /**
     * 缓存消息详情到 Redis
     * @param message 消息对象
     */
    void cacheMessage(MessageRecord message);

    /**
     * 从缓存获取消息详情
     * @param messageId 消息ID
     * @return 消息对象，如果缓存不存在则返回 null
     */
    MessageRecord getCachedMessage(Integer messageId);

    // ========== 群发消息状态追踪 (新增) ==========

    /**
     * 初始化群发消息的接收者状态
     * @param messageId 消息ID
     * @param doctorIds 接收者医生ID列表
     */
    void initMessageReceivers(Integer messageId, List<String> doctorIds);

    /**
     * 初始化单个接收者的状态（用于实时通知场景）
     * 触发器会为每个接收者单独发送通知，所以需要逐个初始化
     * @param messageId 消息ID
     * @param docId 医生ID
     */
    void initSingleReceiverStatus(Integer messageId, String docId);

    /**
     * 标记某个医生已确认收到消息 (read_status: unconfirmed → confirmed)
     * @param messageId 消息ID
     * @param docId 医生ID
     * @return 是否所有接收者都已确认 (用于判断是否更新数据库)
     */
    boolean markMessageSentToDoctor(Integer messageId, String docId);

    /**
     * 检查消息是否已被指定医生确认
     * @param messageId 消息ID
     * @param docId 医生ID
     * @return true=已确认(confirmed), false=未确认(unconfirmed)
     */
    boolean isMessageSentToDoctor(Integer messageId, String docId);

    /**
     * 获取消息的确认进度
     * @param messageId 消息ID
     * @return [已确认数, 总数]，如果消息不存在返回 null
     */
    int[] getMessageSendProgress(Integer messageId);
}
