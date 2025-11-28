package com.example.Config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyContextListener {

    @Autowired
    private SSHConnection sshConnection;

    @PostConstruct
    public void init() {
        log.info("Application context initialized");
        try {
            sshConnection.connect();
            log.info("SSH连接已建立");
        } catch (Exception e) {
            log.error("SSH连接失败: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("Application context is shutting down");
        try {
            sshConnection.close();
            log.info("SSH连接已关闭");
        } catch (Exception e) {
            log.warn("关闭SSH连接时出错: {}", e.getMessage(), e);
        }
    }
}
