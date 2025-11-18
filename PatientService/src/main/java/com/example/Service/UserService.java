package com.example.Service;

import com.example.conmon.result.Result;
import com.example.pojo.dto.LoginRequest;
import com.example.pojo.dto.RegisterRequest;

/**
 * 用户服务接口
 * 定义登录、注册等用户相关操作
 */
public interface UserService {

    /**
     * 用户登录
     * @param loginRequest 包含账号和密码的登录请求体
     * @return 包含 token 或错误信息的结果对象
     */
    Result<String> login(LoginRequest loginRequest);

    /**
     * 用户注册
     * @param registerRequest 包含用户信息的注册请求体
     * @return 包含成功或失败信息的结果对象
     */
    Result<String> register(RegisterRequest registerRequest);

    /**
     * 根据登录账号获取患者ID
     * @param account 登录账号
     * @return 患者ID（如 PAT0001）
     */
    Result<String> getPatientIdByAccount(String account);
}
