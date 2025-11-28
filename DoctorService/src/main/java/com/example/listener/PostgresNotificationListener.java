package com.example.listener;

import com.example.service.DoctorService;
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
 * 监听 add_number_channel，当收到加号申请变更通知时触发 SSE 推送
 */
@Component
public class PostgresNotificationListener {
    
    private static final Logger logger = LoggerFactory.getLogger(PostgresNotificationListener.class);
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private DoctorService doctorService;
    
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
                    stmt.execute("LISTEN add_number_channel");
                    logger.info("✓ 已注册监听通道: add_number_channel");
                }
                
                logger.info("开始轮询数据库通知 (每5秒检查一次)...");
                
                // 持续监听通知
                while (!Thread.currentThread().isInterrupted()) {
                    org.postgresql.PGNotification[] notifications = pgConn.getNotifications(5000);
                    
                    if (notifications != null && notifications.length > 0) {
                        logger.info(">>> 收到 {} 条数据库通知", notifications.length);
                        for (org.postgresql.PGNotification notification : notifications) {
                            String docId = notification.getParameter();
                            logger.info("  - 通道: {}, 医生ID: {}", notification.getName(), docId);
                            
                            // 触发 SSE 推送
                            try {
                                doctorService.notifyAddNumberChange(docId);
                                logger.info("  ✓ SSE推送成功，医生ID: {}", docId);
                            } catch (Exception e) {
                                logger.error("  ✗ SSE推送失败，医生ID: {}", docId, e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("=== 数据库通知监听器异常终止 ===", e);
            }
        }, "postgres-notification-listener").start();
    }
}
