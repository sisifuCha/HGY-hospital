package com.example.utils;

/**
 * 邮箱验证码存储抽象。
 * key 通常为 scene:email。
 */
public interface EmailVerificationCodeStore {

    void save(String key, String code, long expireSeconds);

    String getCode(String key);

    void delete(String key);

    /**
     * 用于发送频率限制：在 cooldownSeconds 内不允许重复发送。
     * @return true 表示成功占位（允许发送），false 表示已在冷却期
     */
    boolean tryAcquireCooldown(String key, long cooldownSeconds);
}

