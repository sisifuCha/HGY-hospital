package com.example.listener;

import com.example.service.DoctorService;
import com.example.service.MessageQueueService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.PGConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * PostgreSQL 数据库通知监听器
 * 监听两个通道：
 * 1. add_number_channel - 加号申请变更通知
 * 2. message_channel - 消息记录变更通知
 */
@Component
public class PostgresNotificationListener {
    
    private static final Logger logger = LoggerFactory.getLogger(PostgresNotificationListener.class);
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private MessageQueueService messageQueueService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 应用启动后自动开始监听数据库通知
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startListening() {
        new Thread(() -> {
            try (Connection conn = dataSource.getConnection()) {
                logger.info("=== PostgreSQL 通知监听器启动 ===");
                logger.info("数据库连接成功: {}", conn.getMetaData().getURL());
                
                // 获取 PostgreSQL 连接并注册监听器
                PGConnection pgConn = conn.unwrap(PGConnection.class);
                
                try (Statement stmt = conn.createStatement()) {
                    // 监听加号申请通道
                    stmt.execute("LISTEN add_number_channel");
                    logger.info("✓ 已注册监听通道: add_number_channel");
                    
                    // 监听消息记录通道
                    stmt.execute("LISTEN message_channel");
                    logger.info("✓ 已注册监听通道: message_channel");
                }
                
                logger.info("开始轮询数据库通知 (每5秒检查一次)...");
                
                // 持续监听通知
                while (!Thread.currentThread().isInterrupted()) {
                    org.postgresql.PGNotification[] notifications = pgConn.getNotifications(5000);
                    
                    if (notifications != null && notifications.length > 0) {
                        logger.info(">>> 收到 {} 条数据库通知", notifications.length);
                        for (org.postgresql.PGNotification notification : notifications) {
                            String channelName = notification.getName();
                            String docId = notification.getParameter();
                            logger.info("  - 通道: {}, 医生ID: {}", channelName, docId);
                            
                            // 根据不同通道处理
                            if ("add_number_channel".equals(channelName)) {
                                handleAddNumberNotification(docId);
                            } else if ("message_channel".equals(channelName)) {
                                handleMessageNotification(docId);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("=== 数据库通知监听器异常终止 ===", e);
            }
        }, "postgres-notification-listener").start();
    }
    
    /**
     * 处理加号申请通知
     */
    private void handleAddNumberNotification(String docId) {
        try {
            // 推送加号申请列表更新 (加号栏刷新)
            // 注意：消息通知由数据库触发器 trg_add_number_insert_message 自动创建
            doctorService.notifyAddNumberChange(docId);
            logger.info("  ✓ 加号申请SSE推送成功，医生ID: {}", docId);
        } catch (Exception e) {
            logger.error("  ✗ 处理加号申请通知失败，医生ID: {}", docId, e);
        }
    }
    
    /**
     * 处理消息记录通知
     * 当触发器触发时，将消息写入 Redis 队列，若前端连接则转发，否则保留在队列中
     * @param payload JSON格式的通知内容，包含 docId 和 messageId
     */
    private void handleMessageNotification(String payload) {
        try {
            // 解析 JSON payload: {"docId": "DOC001", "messageId": 123}
            JsonNode jsonNode = objectMapper.readTree(payload);
            String docId = jsonNode.get("docId").asText();
            Integer messageId = jsonNode.get("messageId").asInt();
            
            logger.info("  → 收到消息通知: 医生ID={}, 消息ID={}", docId, messageId);
            
            // 先将消息入队到 Redis
            messageQueueService.enqueueMessage(docId, messageId);
            
            // 初始化该医生对此消息的状态追踪（单人模式）
            // 触发器会为每个接收者单独发送通知，所以这里逐个初始化
            messageQueueService.initSingleReceiverStatus(messageId, docId);
            
            logger.info("  ✓ 消息已入队到Redis，医生ID: {}, 消息ID: {}", docId, messageId);
            
            // 尝试推送给前端（如果前端已连接）
            doctorService.notifySystemMessage(docId);
            logger.info("  ✓ 消息通知SSE推送成功，医生ID: {}", docId);
        } catch (Exception e) {
            logger.error("  ✗ 处理消息通知失败，payload: {}", payload, e);
        }
    }
}
