package com.example.listener;

import com.example.Mapper.MessageMapper;
import com.example.pojo.dto.MessageDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.PGConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class PostgresNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(PostgresNotificationListener.class);
    private static final String REDIS_KEY_PREFIX = "patient:message:";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @EventListener(ApplicationReadyEvent.class)
    public void startListening() {
        new Thread(() -> {
            try (Connection conn = dataSource.getConnection()) {
                logger.info("=== PostgreSQL 通知监听器启动 (PatientService) ===");
                
                PGConnection pgConn = conn.unwrap(PGConnection.class);
                
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("LISTEN patient_message_channel");
                    logger.info("✓ 已注册监听通道: patient_message_channel");
                }
                
                while (!Thread.currentThread().isInterrupted()) {
                    org.postgresql.PGNotification[] notifications = pgConn.getNotifications(5000);
                    
                    if (notifications != null && notifications.length > 0) {
                        for (org.postgresql.PGNotification notification : notifications) {
                            if ("patient_message_channel".equals(notification.getName())) {
                                handleMessageNotification(notification.getParameter());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("=== 数据库通知监听器异常终止 ===", e);
            }
        }, "postgres-notification-listener").start();
    }

    private void handleMessageNotification(String payload) {
        try {
            // payload: {"patientId": "PAT001", "messageId": 123}
            JsonNode jsonNode = objectMapper.readTree(payload);
            String patientId = jsonNode.get("patientId").asText();
            Long messageId = jsonNode.get("messageId").asLong();
            
            logger.info("收到新消息通知: patientId={}, messageId={}", patientId, messageId);
            
            // 查询消息详情
            MessageDto msg = messageMapper.findMessageById(messageId);
            if (msg != null) {
                String key = REDIS_KEY_PREFIX + patientId;
                // 写入 Redis List
                redisTemplate.opsForList().rightPush(key, msg);
                
                // 标记为已推送到 Redis (可选，如果需要避免重复处理)
                // messageMapper.markAsPushed(messageId); 
                // 注意：这里我们不再需要 markAsPushed，因为是实时触发，不是轮询。
                // 只要保证 Redis 里有就行。
                
                logger.info("消息已写入 Redis: {}", key);
            } else {
                logger.warn("未找到消息记录: id={}", messageId);
            }
            
        } catch (Exception e) {
            logger.error("处理消息通知失败: {}", payload, e);
        }
    }
}
