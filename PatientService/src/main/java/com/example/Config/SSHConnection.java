package com.example.Config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SSHConnection {

    private Session session = null;

    // SSH 连接与端口转发配置常量（已对齐管理端配置）
    private final String SSH_REMOTE_SERVER = "124.71.238.8"; // SSH服务器公网IP（管理端一致）
    private final Integer SSH_REMOTE_PORT = 22; // SSH端口
    private final String SSH_USER = "root"; // SSH用户
    private final String SSH_PASSWORD = "HGY25qiu@shixun"; // SSH密码（管理端一致）

    private final Integer LOCAl_PORT = 63333; // 本地映射端口
    private final String DB_REMOTE_SERVER = "192.168.0.43"; // 远程数据库内网地址（管理端一致）
    private final Integer REMOTE_PORT = 5432; // 远程数据库端口

    public void close() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.info("SSH隧道已关闭");
        }
    }

    public void connect() {
        try {
            if (session != null && session.isConnected()) {
                return; // 已连接无需重复连接
            }
            log.info("正在连接SSH服务器: {}:{}", SSH_REMOTE_SERVER, SSH_REMOTE_PORT);
            JSch jsch = new JSch();
            session = jsch.getSession(SSH_USER, SSH_REMOTE_SERVER, SSH_REMOTE_PORT);
            session.setPassword(SSH_PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(60000);
            session.connect();
            log.info("SSH连接成功");

            // 端口转发
            log.info("建立端口转发: localhost:{} -> {}:{}", LOCAl_PORT, DB_REMOTE_SERVER, REMOTE_PORT);
            session.setPortForwardingL(LOCAl_PORT, DB_REMOTE_SERVER, REMOTE_PORT);
            log.info("端口转发建立成功");
        } catch (Exception e) {
            log.error("SSH连接或端口转发失败: {}", e.getMessage(), e);
        }
    }
}
