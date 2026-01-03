package com.example;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.util.Properties;

/**
 * 独立的 SSH 隧道程序
 * 仅用于维护 SSH 端口转发，不启动 Spring Boot 应用
 */
public class SSHTunnelOnly {
    
    private static final int LOCAL_PORT = 63333;
    private static final int REMOTE_PORT = 5432;
    private static final int SSH_PORT = 22;
    private static final String SSH_USER = "root";
    private static final String SSH_PASSWORD = "HGY25qiu@shixun";
    private static final String SSH_SERVER = "124.71.238.8";
    private static final String DB_SERVER = "192.168.0.43";
    
    public static void main(String[] args) {
        Session session = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(SSH_USER, SSH_SERVER, SSH_PORT);
            session.setPassword(SSH_PASSWORD);
            
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setTimeout(30000);
            
            System.out.println("=== SSH 隧道程序启动 ===");
            System.out.println("正在连接 SSH 服务器: " + SSH_SERVER + ":" + SSH_PORT);
            session.connect();
            System.out.println("✓ SSH 连接成功");
            
            System.out.println("建立端口转发: 0.0.0.0:" + LOCAL_PORT + " -> " + DB_SERVER + ":" + REMOTE_PORT);
            session.setPortForwardingL("0.0.0.0", LOCAL_PORT, DB_SERVER, REMOTE_PORT);
            System.out.println("✓ SSH 隧道已建立");
            System.out.println("\n隧道运行中... 按 Ctrl+C 停止\n");
            
            // 保持程序运行
            final Session finalSession = session;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n正在关闭 SSH 隧道...");
                if (finalSession != null && finalSession.isConnected()) {
                    finalSession.disconnect();
                    System.out.println("✓ SSH 隧道已关闭");
                }
            }));
            
            // 无限等待，直到被中断
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("✗ SSH 隧道失败: " + e.getMessage());
            e.printStackTrace();
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
            System.exit(1);
        }
    }
}
