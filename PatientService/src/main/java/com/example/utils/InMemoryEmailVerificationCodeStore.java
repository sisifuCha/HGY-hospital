package com.example.utils;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试/开发兜底用的内存验证码存储：
 * - 避免本机未启动 Redis 时，邮箱验证码相关功能无法自测/跑测试
 * - 生产环境不启用（通过 Spring profile 控制）
 */
@Component
@Primary
@Profile({"test", "local"})
public class InMemoryEmailVerificationCodeStore implements EmailVerificationCodeStore {

    private static class CodeEntry {
        final String code;
        final long expireAtMillis;

        CodeEntry(String code, long expireAtMillis) {
            this.code = code;
            this.expireAtMillis = expireAtMillis;
        }

        boolean expired() {
            return Instant.now().toEpochMilli() > expireAtMillis;
        }
    }

    private final Map<String, CodeEntry> codes = new ConcurrentHashMap<>();
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    @Override
    public void save(String key, String code, long expireSeconds) {
        long expireAt = Instant.now().toEpochMilli() + expireSeconds * 1000;
        codes.put(key, new CodeEntry(code, expireAt));
    }

    @Override
    public String getCode(String key) {
        CodeEntry entry = codes.get(key);
        if (entry == null) return null;
        if (entry.expired()) {
            codes.remove(key);
            return null;
        }
        return entry.code;
    }

    @Override
    public void delete(String key) {
        codes.remove(key);
    }

    @Override
    public boolean tryAcquireCooldown(String key, long cooldownSeconds) {
        long now = Instant.now().toEpochMilli();
        long until = now + cooldownSeconds * 1000;

        Long existing = cooldowns.putIfAbsent(key, until);
        if (existing == null) {
            return true;
        }
        if (existing <= now) {
            cooldowns.put(key, until);
            return true;
        }
        return false;
    }
}

