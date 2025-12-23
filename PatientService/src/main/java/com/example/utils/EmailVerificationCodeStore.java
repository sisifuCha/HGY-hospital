package com.example.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码存储（基于 Redis）
 */
@Component
public class EmailVerificationCodeStore {

    private final StringRedisTemplate redisTemplate;

    private static final String COOLDOWN_SUFFIX = ":cooldown";

    public EmailVerificationCodeStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取发送冷却锁
     * @param key 验证码 Key
     * @param cooldownSeconds 冷却时间（秒）
     * @return true 表示可以发送，false 表示仍在冷却中
     */
    public boolean tryAcquireCooldown(String key, long cooldownSeconds) {
        String cooldownKey = key + COOLDOWN_SUFFIX;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(cooldownKey, "1", cooldownSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 保存验证码
     * @param key 验证码 Key
     * @param code 验证码
     * @param expireSeconds 过期时间（秒）
     */
    public void save(String key, String code, long expireSeconds) {
        redisTemplate.opsForValue().set(key, code, expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 获取验证码
     * @param key 验证码 Key
     * @return 验证码，不存在或已过期返回 null
     */
    public String getCode(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除验证码（验证成功后调用）
     * @param key 验证码 Key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
        redisTemplate.delete(key + COOLDOWN_SUFFIX);
    }
}
