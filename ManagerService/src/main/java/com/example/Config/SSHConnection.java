package com.example.Config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.util.Properties;

public class SSHConnection {

    // 使用密码认证，不需要私钥文件
    // private final static String S_PATH_FILE_PRIVATE_KEY = "";
    private final static String S_PATH_FILE_KNOWN_HOSTS = ""; // 不使用known_hosts文件
    private final static String S_PASS_PHRASE = ""; // 无密钥密码
    private final static int LOCAl_PORT = 63333; // 本地隧道端口
    private final static int REMOTE_PORT = 5432; // 数据库端口
    private final static int SSH_REMOTE_PORT = 22; // SSH服务器端口
    private final static String SSH_USER = "root"; // SSH用户名
    private final static String SSH_PASSWORD = "HGY25qiu@shixun"; // SSH密码
    private final static String SSH_REMOTE_SERVER = "124.71.238.8"; // SSH服务器公网IP

    // 数据库内网地址
    private final static String DB_REMOTE_SERVER = "192.168.0.43";

    private Session session; // 代表SSH会话

    public void closeSSH() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("SSH隧道已关闭");
        }
    }

    public SSHConnection() throws Throwable {
        JSch jsch = new JSch();

        // 不使用known_hosts文件，跳过主机密钥检查
        // jsch.setKnownHosts(S_PATH_FILE_KNOWN_HOSTS);

        // 不使用密钥认证
        // jsch.addIdentity(S_PATH_FILE_PRIVATE_KEY, S_PASS_PHRASE);

        // 创建SSH会话
        session = jsch.getSession(SSH_USER, SSH_REMOTE_SERVER, SSH_REMOTE_PORT);
        session.setPassword(SSH_PASSWORD);

        // 配置跳过主机密钥检查
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        // 设置连接超时时间（30秒）
        session.setTimeout(30000);

        // 建立SSH连接
        System.out.println("正在连接SSH服务器: " + SSH_REMOTE_SERVER + ":" + SSH_REMOTE_PORT);
        session.connect();
        System.out.println("SSH连接成功!");

        // 设置本地端口转发
        System.out.println("建立端口转发: localhost:" + LOCAl_PORT + " -> " + DB_REMOTE_SERVER + ":" + REMOTE_PORT);
        session.setPortForwardingL(LOCAl_PORT, DB_REMOTE_SERVER, REMOTE_PORT);
        System.out.println("端口转发建立成功!");
    }
}