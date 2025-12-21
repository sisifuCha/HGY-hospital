package com.example.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 存储实现：
 * - code: key -> 6位验证码，TTL=expireSeconds
 * - cooldown: cooldownKey -> "1"，TTL=cooldownSeconds
 */
@Component
public class RedisEmailVerificationCodeStore implements EmailVerificationCodeStore {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${patient.emailVerification.redisKeyPrefix:email:verify:}")
    private String prefix;

    public RedisEmailVerificationCodeStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String key, String code, long expireSeconds) {
        redisTemplate.opsForValue().set(buildCodeKey(key), code, Duration.ofSeconds(expireSeconds));
    }

    @Override
    public String getCode(String key) {
        Object v = redisTemplate.opsForValue().get(buildCodeKey(key));
        if (v == null) return null;
        return String.valueOf(v);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(buildCodeKey(key));
        // cooldown key 不强制删除，让它自然过期即可
    }

    @Override
    public boolean tryAcquireCooldown(String key, long cooldownSeconds) {
        String cooldownKey = buildCooldownKey(key);
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(cooldownKey, "1", Duration.ofSeconds(cooldownSeconds));
        return Boolean.TRUE.equals(ok);
    }

    private String buildCodeKey(String key) {
        return prefix + "code:" + key;
    }

    private String buildCooldownKey(String key) {
        return prefix + "cooldown:" + key;
    }
}

