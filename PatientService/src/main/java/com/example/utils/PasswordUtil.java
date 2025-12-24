package com.example.utils;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密工具类
 * 使用SHA-256和盐值进行密码加密
 */
@Component
public class PasswordUtil {

    private static final int SALT_LENGTH = 16;
    private static final String ALGORITHM = "SHA-256";

    /**
     * 生成随机盐值
     * @return Base64编码的盐值
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 使用盐值对密码进行哈希
     * @param password 原始密码
     * @param salt 盐值
     * @return 哈希后的密码
     */
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密算法不可用", e);
        }
    }

    /**
     * 加密密码（生成盐值并哈希）
     * @param plainPassword 原始密码
     * @return 格式为 "盐值:哈希密码" 的字符串
     */
    public String encryptPassword(String plainPassword) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(plainPassword, salt);
        return salt + ":" + hashedPassword;
    }

    /**
     * 验证密码
     * @param plainPassword 用户输入的原始密码
     * @param encryptedPassword 存储的加密密码 (格式: "盐值:哈希密码")
     * @return 密码是否匹配
     */
    public boolean verifyPassword(String plainPassword, String encryptedPassword) {
        if (encryptedPassword == null || !encryptedPassword.contains(":")) {
            // 兼容旧的明文密码（迁移期间）
            return plainPassword.equals(encryptedPassword);
        }
        
        String[] parts = encryptedPassword.split(":", 2);
        String salt = parts[0];
        String storedHash = parts[1];
        String inputHash = hashPassword(plainPassword, salt);
        return storedHash.equals(inputHash);
    }
}
