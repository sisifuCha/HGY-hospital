package com.example.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;

/**
 * 请求日志拦截器
 * 记录所有请求的详细信息到控制台
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 打印请求基本信息
        System.out.println("\n===== 请求日志开始 =====");
        System.out.println("请求方法: " + request.getMethod());
        System.out.println("请求URL: " + request.getRequestURL().toString());
        System.out.println("请求URI: " + request.getRequestURI());
        System.out.println("客户端IP: " + request.getRemoteAddr());
        System.out.println("用户代理: " + request.getHeader("User-Agent"));
        
        // 打印请求头信息
        System.out.println("\n请求头信息:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            System.out.println(headerName + ": " + headerValue);
        }
        
        // 打印请求参数信息
        System.out.println("\n请求参数信息:");
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            System.out.print(paramName + ": ");
            if (paramValues.length == 1) {
                System.out.println(paramValues[0]);
            } else {
                System.out.print("[");
                for (int i = 0; i < paramValues.length; i++) {
                    System.out.print(paramValues[i]);
                    if (i < paramValues.length - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println("]");
            }
        }
        
        System.out.println("===== 请求日志结束 =====\n");
        
        // 继续处理请求
        return true;
    }
}