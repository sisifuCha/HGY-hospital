package com.example.Controller;

import com.example.pojo.dto.LoginRequestDTO;
import com.example.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 从前端获取密码，和写死的密码对比，成功则返回jwt，不成功返回失败信息
 */
@RestController //自动将返回值转为JSON
public class LoginController {

    // 正确的密码常量
    private static final String CORRECT_PASSWORD = "082109Zhr";

    @PostMapping(value = "/admin/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequestDTO loginRequestDTO) {


        // 创建响应对象
        Map<String, Object> response = new HashMap<>();

        // 检查密码是否为空
        if (!StringUtils.hasText(loginRequestDTO.getPassword())) {
            response.put("code", 400);
            response.put("data", null);
            response.put("msg", "密码不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 验证密码
        if (CORRECT_PASSWORD.equals(loginRequestDTO.getPassword())) {
            // 密码正确，生成JWT令牌
            try {
                String jwtToken = JwtUtil.generateToken("user"); // 可以根据需要传入用户名

                response.put("code", 200);
                response.put("data", jwtToken); // JWT令牌放在data字段
                response.put("msg", "登录成功");

                System.out.println("登录成功，生成的JWT: " + jwtToken);
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                // JWT生成失败
                response.put("code", 500);
                response.put("data", null);
                response.put("msg", "系统错误，令牌生成失败");
                return ResponseEntity.internalServerError().body(response);
            }
        } else {
            // 密码错误
            response.put("code", 401);
            response.put("data", null);
            response.put("msg", "fail");

            System.out.println("登录失败，输入密码: " + loginRequestDTO.getPassword());
            return ResponseEntity.status(401).body(response);
        }
    }
}