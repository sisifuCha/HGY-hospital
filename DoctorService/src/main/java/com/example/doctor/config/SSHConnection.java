package com.example.doctor.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Properties;

@Component
public class SSHConnection {

    private final static int LOCAl_PORT = 63333;
    private final static int REMOTE_PORT = 5432;
    private final static int SSH_REMOTE_PORT = 22;
    private final static String SSH_USER = "root";
    private final static String SSH_PASSWORD = "HGY25qiu@shixun";
    private final static String SSH_REMOTE_SERVER = "124.71.238.8";
    private final static String DB_REMOTE_SERVER = "192.168.0.43";

    private Session session;

    public SSHConnection() throws Throwable {
        JSch jsch = new JSch();
        session = jsch.getSession(SSH_USER, SSH_REMOTE_SERVER, SSH_REMOTE_PORT);
        session.setPassword(SSH_PASSWORD);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.setTimeout(30000);

        System.out.println("正在连接SSH服务器: " + SSH_REMOTE_SERVER + ":" + SSH_REMOTE_PORT);
        session.connect();
        System.out.println("SSH连接成功!");

        System.out.println("建立端口转发: localhost:" + LOCAl_PORT + " -> " + DB_REMOTE_SERVER + ":" + REMOTE_PORT);
        session.setPortForwardingL(LOCAl_PORT, DB_REMOTE_SERVER, REMOTE_PORT);
        System.out.println("端口转发建立成功!");
    }

    @PreDestroy
    public void closeSSH() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("SSH隧道已关闭");
        }
    }
}