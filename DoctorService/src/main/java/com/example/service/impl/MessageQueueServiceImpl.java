package com.example.service.impl;

import com.example.entity.MessageRecord;
import com.example.mapper.DoctorMapper;
import com.example.mapper.MessageRecordMapper;
import com.example.service.MessageQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 消息队列服务实现类
 */
@Service
public class MessageQueueServiceImpl implements MessageQueueService {

    private static final Logger logger = LoggerFactory.getLogger(MessageQueueServiceImpl.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MessageRecordMapper messageRecordMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    // Redis Key 前缀
    private static final String QUEUE_KEY_PREFIX = "notification:queue:doctor:";
    private static final String MESSAGE_CACHE_PREFIX = "notification:message:";
    private static final String MESSAGE_STATUS_PREFIX = "notification:status:";  // 消息-医生状态
    private static final String MESSAGE_COUNT_PREFIX = "notification:count:";    // 消息发送计数
    private static final long MESSAGE_CACHE_TTL = 1; // 消息缓存过期时间（小时）
    private static final long MESSAGE_STATUS_TTL = 24; // 状态缓存过期时间（小时）

    /**
     * 应用启动时自动初始化消息队列
     * 从数据库加载所有未发送的消息到 Redis 队列
     */
    @EventListener(ApplicationReadyEvent.class)
    @Override
    public void initializeQueue() {
        logger.info("=== 初始化消息队列 ===");
        try {
            // 先清空所有旧的消息队列，避免重复
            clearAllQueues();
            
            // 从数据库加载所有未发送的消息
            List<MessageRecord> unsentMessages = messageRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MessageRecord>()
                    .eq("status", "unsent")
                    .eq("read_status", "unconfirmed")
            );

            if (unsentMessages == null || unsentMessages.isEmpty()) {
                logger.info("没有待发送消息需要加载");
                return;
            }

            logger.info("从数据库加载到 {} 条未发送消息", unsentMessages.size());

            // 处理每条消息
            for (MessageRecord msg : unsentMessages) {
                String receiverType = msg.getReceiverType();
                List<String> targetDoctorIds = new ArrayList<>();
                
                if ("specific_doctor".equals(receiverType)) {
                    // 发送给特定医生
                    String docId = msg.getReceiverId();
                    if (docId != null) {
                        targetDoctorIds.add(docId);
                    }
                    
                } else if ("department_doctors".equals(receiverType)) {
                    // 发送给科室所有医生
                    String departId = msg.getReceiverId();
                    if (departId != null) {
                        targetDoctorIds = doctorMapper.selectDoctorIdsByDepartment(departId);
                    }
                    
                } else if ("all_doctors".equals(receiverType)) {
                    // 发送给所有医生
                    targetDoctorIds = doctorMapper.selectAllDoctorIds();
                }
                
                // 为每个目标医生入队并初始化状态追踪
                if (!targetDoctorIds.isEmpty()) {
                    for (String docId : targetDoctorIds) {
                        enqueueMessage(docId, msg.getId());
                    }
                    // 初始化群发状态追踪
                    initMessageReceivers(msg.getId(), targetDoctorIds);
                    cacheMessage(msg);
                    logger.info("  → 消息 {} ({}) 入队给 {} 名医生", 
                        msg.getId(), receiverType, targetDoctorIds.size());
                }
            }

            logger.info("=== 消息队列初始化完成 ===");
        } catch (Exception e) {
            logger.error("初始化消息队列失败", e);
        }
    }

    @Override
    public void enqueueMessage(String docId, Integer messageId) {
        if (docId == null || messageId == null) {
            return;
        }
        String queueKey = QUEUE_KEY_PREFIX + docId;
        redisTemplate.opsForList().rightPush(queueKey, messageId);
        logger.debug("消息 {} 已加入医生 {} 的队列", messageId, docId);
    }

    @Override
    public void enqueueMessages(String docId, List<Integer> messageIds) {
        if (docId == null || messageIds == null || messageIds.isEmpty()) {
            return;
        }
        String queueKey = QUEUE_KEY_PREFIX + docId;
        messageIds.forEach(msgId -> redisTemplate.opsForList().rightPush(queueKey, msgId));
        logger.debug("批量加入 {} 条消息到医生 {} 的队列", messageIds.size(), docId);
    }

    @Override
    public List<MessageRecord> getQueuedMessages(String docId) {
        if (docId == null) {
            return new ArrayList<>();
        }
        
        String queueKey = QUEUE_KEY_PREFIX + docId;
        Long size = redisTemplate.opsForList().size(queueKey);
        
        if (size == null || size == 0) {
            return new ArrayList<>();
        }

        // 获取队列中的所有消息ID
        List<Object> messageIdObjs = redisTemplate.opsForList().range(queueKey, 0, -1);
        if (messageIdObjs == null || messageIdObjs.isEmpty()) {
            return new ArrayList<>();
        }

        List<MessageRecord> messages = new ArrayList<>();
        for (Object msgIdObj : messageIdObjs) {
            // 处理不同类型的ID (可能是 Integer, Long, 或 String)
            Integer msgId;
            try {
                if (msgIdObj instanceof Integer) {
                    msgId = (Integer) msgIdObj;
                } else if (msgIdObj instanceof Long) {
                    msgId = ((Long) msgIdObj).intValue();
                } else if (msgIdObj instanceof String) {
                    msgId = Integer.parseInt((String) msgIdObj);
                } else if (msgIdObj instanceof Number) {
                    msgId = ((Number) msgIdObj).intValue();
                } else {
                    logger.warn("无法识别的消息ID类型: {}", msgIdObj.getClass().getName());
                    continue;
                }
            } catch (Exception e) {
                logger.warn("解析消息ID失败: {}", msgIdObj, e);
                continue;
            }
            
            // 先尝试从缓存获取
            MessageRecord cached = getCachedMessage(msgId);
            if (cached != null) {
                messages.add(cached);
            } else {
                // 缓存未命中，从数据库查询
                MessageRecord dbMsg = messageRecordMapper.selectById(msgId);
                if (dbMsg != null) {
                    messages.add(dbMsg);
                    cacheMessage(dbMsg); // 缓存消息
                }
            }
        }

        return messages;
    }

    @Override
    public void dequeueMessage(String docId, Integer messageId) {
        if (docId == null || messageId == null) {
            return;
        }
        String queueKey = QUEUE_KEY_PREFIX + docId;
        redisTemplate.opsForList().remove(queueKey, 1, messageId);
        logger.debug("消息 {} 已从医生 {} 的队列移除", messageId, docId);
    }

    @Override
    public void dequeueMessages(String docId, List<Integer> messageIds) {
        if (docId == null || messageIds == null || messageIds.isEmpty()) {
            return;
        }
        messageIds.forEach(msgId -> dequeueMessage(docId, msgId));
        logger.debug("批量移除 {} 条消息从医生 {} 的队列", messageIds.size(), docId);
    }

    @Override
    public void clearQueue(String docId) {
        if (docId == null) {
            return;
        }
        String queueKey = QUEUE_KEY_PREFIX + docId;
        redisTemplate.delete(queueKey);
        logger.debug("已清空医生 {} 的消息队列", docId);
    }

    @Override
    public Long getQueueSize(String docId) {
        if (docId == null) {
            return 0L;
        }
        String queueKey = QUEUE_KEY_PREFIX + docId;
        Long size = redisTemplate.opsForList().size(queueKey);
        return size != null ? size : 0L;
    }

    @Override
    public void cacheMessage(MessageRecord message) {
        if (message == null || message.getId() == null) {
            return;
        }
        String cacheKey = MESSAGE_CACHE_PREFIX + message.getId();
        redisTemplate.opsForValue().set(cacheKey, message, MESSAGE_CACHE_TTL, TimeUnit.HOURS);
    }

    @Override
    public MessageRecord getCachedMessage(Integer messageId) {
        if (messageId == null) {
            return null;
        }
        String cacheKey = MESSAGE_CACHE_PREFIX + messageId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        return cached != null ? (MessageRecord) cached : null;
    }

    /**
     * 清空所有消息队列
     * 在应用启动时调用，避免残留数据导致重复
     */
    private void clearAllQueues() {
        try {
            // 删除所有 notification:queue:doctor:* 的 key
            java.util.Set<String> keys = redisTemplate.keys(QUEUE_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.info("清空了 {} 个旧消息队列", keys.size());
            }
            
            // 删除所有消息缓存
            java.util.Set<String> cacheKeys = redisTemplate.keys(MESSAGE_CACHE_PREFIX + "*");
            if (cacheKeys != null && !cacheKeys.isEmpty()) {
                redisTemplate.delete(cacheKeys);
                logger.info("清空了 {} 个消息缓存", cacheKeys.size());
            }
            
            // 删除所有消息状态
            java.util.Set<String> statusKeys = redisTemplate.keys(MESSAGE_STATUS_PREFIX + "*");
            if (statusKeys != null && !statusKeys.isEmpty()) {
                redisTemplate.delete(statusKeys);
                logger.info("清空了 {} 个消息状态", statusKeys.size());
            }
            
            // 删除所有消息计数
            java.util.Set<String> countKeys = redisTemplate.keys(MESSAGE_COUNT_PREFIX + "*");
            if (countKeys != null && !countKeys.isEmpty()) {
                redisTemplate.delete(countKeys);
                logger.info("清空了 {} 个消息计数", countKeys.size());
            }
        } catch (Exception e) {
            logger.warn("清空旧队列失败，继续初始化", e);
        }
    }

    // ========== 群发消息状态追踪实现 ==========

    @Override
    public void initMessageReceivers(Integer messageId, List<String> doctorIds) {
        if (messageId == null || doctorIds == null || doctorIds.isEmpty()) {
            return;
        }
        
        String statusKey = MESSAGE_STATUS_PREFIX + messageId;
        String countKey = MESSAGE_COUNT_PREFIX + messageId;
        
        // 为每个医生设置初始 read_status 为 "unconfirmed"
        for (String docId : doctorIds) {
            redisTemplate.opsForHash().put(statusKey, docId, "unconfirmed");
        }
        
        // 设置计数器
        redisTemplate.opsForHash().put(countKey, "total", doctorIds.size());
        redisTemplate.opsForHash().put(countKey, "confirmed", 0);
        
        // 设置过期时间
        redisTemplate.expire(statusKey, MESSAGE_STATUS_TTL, TimeUnit.HOURS);
        redisTemplate.expire(countKey, MESSAGE_STATUS_TTL, TimeUnit.HOURS);
        
        logger.debug("初始化消息 {} 的接收者状态，共 {} 人", messageId, doctorIds.size());
    }

    @Override
    public void initSingleReceiverStatus(Integer messageId, String docId) {
        if (messageId == null || docId == null) {
            return;
        }
        
        String statusKey = MESSAGE_STATUS_PREFIX + messageId;
        String countKey = MESSAGE_COUNT_PREFIX + messageId;
        
        // 检查该医生是否已初始化（避免重复）
        Object existingStatus = redisTemplate.opsForHash().get(statusKey, docId);
        if (existingStatus != null) {
            // 已存在，跳过
            return;
        }
        
        // 设置该医生的 read_status 为 "unconfirmed"
        redisTemplate.opsForHash().put(statusKey, docId, "unconfirmed");
        
        // 增加 total 计数器（使用 increment 保证原子性）
        redisTemplate.opsForHash().increment(countKey, "total", 1);
        
        // 确保 confirmed 计数器存在
        if (redisTemplate.opsForHash().get(countKey, "confirmed") == null) {
            redisTemplate.opsForHash().put(countKey, "confirmed", 0);
        }
        
        // 设置/刷新过期时间
        redisTemplate.expire(statusKey, MESSAGE_STATUS_TTL, TimeUnit.HOURS);
        redisTemplate.expire(countKey, MESSAGE_STATUS_TTL, TimeUnit.HOURS);
        
        logger.debug("初始化消息 {} 对医生 {} 的接收状态", messageId, docId);
    }

    @Override
    public boolean markMessageSentToDoctor(Integer messageId, String docId) {
        if (messageId == null || docId == null) {
            return false;
        }
        
        String statusKey = MESSAGE_STATUS_PREFIX + messageId;
        String countKey = MESSAGE_COUNT_PREFIX + messageId;
        
        // 检查是否已确认过
        Object currentStatus = redisTemplate.opsForHash().get(statusKey, docId);
        if ("confirmed".equals(currentStatus)) {
            return false; // 已经确认过，不重复计数
        }
        
        // 更新 read_status 为 confirmed
        redisTemplate.opsForHash().put(statusKey, docId, "confirmed");
        
        // 增加已确认计数
        Long confirmedCount = redisTemplate.opsForHash().increment(countKey, "confirmed", 1);
        
        // 获取总数，判断是否全部确认完成
        Object totalObj = redisTemplate.opsForHash().get(countKey, "total");
        int total = 0;
        if (totalObj instanceof Integer) {
            total = (Integer) totalObj;
        } else if (totalObj instanceof Long) {
            total = ((Long) totalObj).intValue();
        } else if (totalObj instanceof Number) {
            total = ((Number) totalObj).intValue();
        }
        
        boolean allConfirmed = confirmedCount != null && confirmedCount >= total && total > 0;
        
        if (allConfirmed) {
            logger.info("消息 {} 已被所有接收者确认 ({}/{})", messageId, confirmedCount, total);
        }
        
        return allConfirmed;
    }

    @Override
    public boolean isMessageSentToDoctor(Integer messageId, String docId) {
        if (messageId == null || docId == null) {
            return false;
        }
        
        String statusKey = MESSAGE_STATUS_PREFIX + messageId;
        Object status = redisTemplate.opsForHash().get(statusKey, docId);
        return "confirmed".equals(status);
    }

    @Override
    public int[] getMessageSendProgress(Integer messageId) {
        if (messageId == null) {
            return null;
        }
        
        String countKey = MESSAGE_COUNT_PREFIX + messageId;
        Object confirmedObj = redisTemplate.opsForHash().get(countKey, "confirmed");
        Object totalObj = redisTemplate.opsForHash().get(countKey, "total");
        
        if (confirmedObj == null || totalObj == null) {
            return null;
        }
        
        int confirmed = confirmedObj instanceof Number ? ((Number) confirmedObj).intValue() : 0;
        int total = totalObj instanceof Number ? ((Number) totalObj).intValue() : 0;
        
        return new int[]{confirmed, total};
    }
}
