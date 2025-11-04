package com.example.utils;

import com.example.Conmon.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.utils.JwtUtil;

import java.io.PrintWriter;

@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 1. 从请求头中获取Token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            // 使用工具方法发送错误响应
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), "缺少有效的Token");
            return false;
        }
        // 去掉"Bearer "前缀
        token = token.substring(7);

        // 2. 验证Token
        try {
            // 假设JwtUtil.parseJWT返回Claims对象
            return JwtUtil.parseJWT(token);
        } catch (Exception e) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), "Token无效或已过期");
            return false;
        }
    }

    /**
     * 统一的错误响应方法
     * 设置正确的Content-Type和字符编码，并将Result对象序列化为JSON字符串
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws Exception {
        // 设置状态码
        response.setStatus(statusCode);
        // 设置内容类型和字符编码，这是关键步骤[2,3](@ref)
        response.setContentType("application/json;charset=UTF-8");

        // 创建统一的Result对象
        Result<String> result = Result.fail(message);

        // 使用Jackson的ObjectMapper将对象序列化为JSON字符串[5,6](@ref)
        // 确保您的项目中引入了jackson-databind依赖
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(result);

        // 将JSON字符串写入响应流
        PrintWriter out = response.getWriter();
        out.print(jsonResponse);
        out.flush();
    }
}