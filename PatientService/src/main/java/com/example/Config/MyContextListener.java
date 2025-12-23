package com.example.Config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyContextListener {

    @Autowired
    private SSHConnection sshConnection;

    @Value("${patient.sshTunnel.enabled:true}")
    private boolean sshTunnelEnabled;

    @PostConstruct
    public void init() {
        log.info("Application context initialized");
        if (!sshTunnelEnabled) {
            log.info("SSH tunnel disabled by config (patient.sshTunnel.enabled=false)");
            return;
        }
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
