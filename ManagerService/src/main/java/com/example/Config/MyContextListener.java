package com.example.Config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class MyContextListener implements ServletContextListener {

    private SSHConnection conexionssh;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Context initialized ... !");
        try {
            conexionssh = new SSHConnection();
            System.out.println("SSH连接已建立");
        } catch (Throwable e) {
            System.err.println("SSH连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Context destroyed ... !");
        if (conexionssh != null) {
            conexionssh.closeSSH();
            System.out.println("SSH连接已关闭");
        }
    }
}