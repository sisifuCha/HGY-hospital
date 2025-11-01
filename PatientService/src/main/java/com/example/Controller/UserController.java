package com.example.Controller;

import com.example.Service.UserService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.LoginRequest;
import com.example.pojo.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterRequest registerRequest) {
        return userService.register(registerRequest);
    }
}

