package com.example.Service;

import com.example.conmon.result.Result;
import com.example.pojo.dto.PasswordResetConfirmRequest;
import com.example.pojo.dto.PasswordResetConfirmResponse;
import com.example.pojo.dto.PasswordResetSendRequest;
import com.example.pojo.dto.PasswordResetSendResponse;

public interface PasswordResetService {
    Result<PasswordResetSendResponse> sendResetCode(PasswordResetSendRequest request);

    Result<PasswordResetConfirmResponse> confirmReset(PasswordResetConfirmRequest request);
}

