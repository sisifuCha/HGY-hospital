package com.example.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 最基础的验证码存储实现：写入本地 JSON 文件。
 *
 * 注意：
 * - 适合开发/演示环境；生产建议换 Redis。
 * - 为避免并发写坏文件，这里采用“写临时文件再原子替换”的方式。
 */
@Component
public class EmailVerificationStore {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 存储文件：在当前工作目录下生成（通常是 PatientService 模块目录）。
     */
    private final Path storePath = Path.of("email_verification_codes.json");

    public synchronized void save(String key, String code, long expireAtEpochSeconds) {
        Map<String, CodeRecord> map = readAllInternal();
        map.put(key, new CodeRecord(code, expireAtEpochSeconds));
        writeAllInternal(map);
    }

    public synchronized CodeRecord get(String key) {
        Map<String, CodeRecord> map = readAllInternal();
        return map.get(key);
    }

    public synchronized void delete(String key) {
        Map<String, CodeRecord> map = readAllInternal();
        if (map.remove(key) != null) {
            writeAllInternal(map);
        }
    }

    private Map<String, CodeRecord> readAllInternal() {
        if (!Files.exists(storePath)) {
            return new HashMap<>();
        }
        try {
            byte[] bytes = Files.readAllBytes(storePath);
            if (bytes.length == 0) {
                return new HashMap<>();
            }
            return MAPPER.readValue(bytes, new TypeReference<Map<String, CodeRecord>>() {});
        } catch (Exception e) {
            // 文件损坏或不兼容时，降级为空，避免影响服务启动
            return new HashMap<>();
        }
    }

    private void writeAllInternal(Map<String, CodeRecord> map) {
        try {
            Path tmp = Path.of(storePath.toString() + ".tmp");
            byte[] bytes = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(map);
            Files.write(tmp, bytes);
            Files.move(tmp, storePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write verification store", e);
        }
    }

    public static String buildKey(String email, String scene) {
        String s = (scene == null || scene.isBlank()) ? "REGISTER" : scene.trim().toUpperCase();
        return s + ":" + email.trim().toLowerCase();
    }

    public static long nowEpochSeconds() {
        return Instant.now().getEpochSecond();
    }

    public record CodeRecord(String code, long expireAt) {}
}

