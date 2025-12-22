package com.example.Service;

import com.example.Mapper.MessageMapper;
import com.example.pojo.dto.MessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "patient.message.task", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MessageTaskService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "patient:message:";

    // 1. 定时从数据库读取 unsent 消息写入 Redis -> 已移除，改为 PostgreSQL 触发器 + 监听器模式
    // 详见 com.example.listener.PostgresNotificationListener
    
    // 2. 定时清理 Redis 中已发送的消息并回写数据库
    @Scheduled(fixedRate = 10000) // 每10秒执行一次
    public void syncSentMessagesToDB() {
        Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                // 获取所有消息
                List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);
                if (messages != null) {
                    for (Object obj : messages) {
                        MessageDto msg = (MessageDto) obj;
                        if ("sent".equals(msg.getStatus())) {
                            // 回写数据库
                            messageMapper.updateMessageStatus(msg.getId(), "sent");
                            // 从 Redis 移除 (这里简化处理，实际可能需要精确移除)
                            redisTemplate.opsForList().remove(key, 1, msg);
                        }
                    }
                }
            }
        }
    }
}
