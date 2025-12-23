package com.example.Controller;

import com.example.Service.PasswordResetService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.PasswordResetConfirmRequest;
import com.example.pojo.dto.PasswordResetConfirmResponse;
import com.example.pojo.dto.PasswordResetSendRequest;
import com.example.pojo.dto.PasswordResetSendResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/send")
    public Result<PasswordResetSendResponse> send(@RequestBody PasswordResetSendRequest request) {
        return passwordResetService.sendResetCode(request);
    }

    @PostMapping("/confirm")
    public Result<PasswordResetConfirmResponse> confirm(@RequestBody PasswordResetConfirmRequest request) {
        return passwordResetService.confirmReset(request);
    }
}

