package com.example.Config;

import com.example.utils.JwtAuthenticationInterceptor;
import com.example.utils.RequestLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;
    
    @Autowired
    private RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册请求日志拦截器，拦截所有请求
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**"); // 拦截所有请求
        
        // 注册JWT认证拦截器
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/admin/**") // 拦截所有/admin开头的请求
                .excludePathPatterns("/admin/login"); // 排除登录注册接口
    }
}