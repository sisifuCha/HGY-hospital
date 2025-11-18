package com.example.Controller;

import com.example.Service.UserService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.LoginRequest;
import com.example.pojo.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest loginRequest) {
        try {
            return userService.login(loginRequest);
        } catch (IllegalArgumentException ex) {
            return Result.fail(401, "密码错误");
        } catch (Exception ex) {
            return Result.fail(404, "用户不存在");
        }
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest registerRequest) {
        try {
            return userService.register(registerRequest);
        } catch (IllegalArgumentException ex) {
            return Result.fail(409, "账户已存在");
        } catch (Exception ex) {
            return Result.fail(400, "注册失败");
        }
    }

    @GetMapping("/patient-id")
    public Result<String> getPatientId(@RequestParam("account") String account) {
        return userService.getPatientIdByAccount(account);
    }
}
